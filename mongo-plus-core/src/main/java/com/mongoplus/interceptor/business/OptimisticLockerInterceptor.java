package com.mongoplus.interceptor.business;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.UpdateResult;
import com.mongoplus.annotation.collection.Version;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.domain.OptimisticLockerException;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.enums.UpdateConditionEnum;
import com.mongoplus.interceptor.AdvancedInterceptor;
import com.mongoplus.interceptor.Invocation;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.MutablePair;
import com.mongoplus.model.Retry;
import com.mongoplus.model.UpdateRetryResult;
import com.mongoplus.registry.MongoEntityMappingRegistry;
import org.bson.BsonDocument;
import org.bson.BsonNumber;
import org.bson.Document;
import org.bson.codecs.DecoderContext;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.mongoplus.enums.ExecuteMethodEnum.*;
import static com.mongoplus.enums.UpdateConditionEnum.INC;

/**
 * 乐观锁
 * @author anwen
 */
public class OptimisticLockerInterceptor implements AdvancedInterceptor {

    private final Log log = LogFactory.getLog(OptimisticLockerInterceptor.class);

    private final Map<Class<?>,FieldInformation> optimisticLockerExistMap = new ConcurrentHashMap<>();

    /**
     * 字段值为空时需要抛出的异常
     */
    private RuntimeException versionIsNullException;

    /**
     * 更新失败需要抛出的异常
     */
    private RuntimeException updateFailException;

    /**
     * 自增数，默认为1
     */
    private Integer autoInc = 1;

    /**
     * 重试策略
     */
    private Retry retry;

    /**
     * 设置乐观锁字段自增数
     * @author anwen
     */
    public void setAutoInc(Integer autoInc){
        this.autoInc = autoInc;
    }

    /**
     * 设置字段值为空时，需要抛出的异常。
     * 作用于整个集合，对目标集合进行`修改`时，如果目标集合存在锁字段(集合所对应的实体类)，则修改必须传值
     * @param versionIsNullException 异常
     * @author anwen
     */
    public void setVersionIsNullException(RuntimeException versionIsNullException) {
        this.versionIsNullException = versionIsNullException;
    }

    /**
     * 更新失败需要抛出的异常
     * @param updateFailException 异常
     * @author anwen
     */
    public void setUpdateFailException(RuntimeException updateFailException) {
        this.updateFailException = updateFailException;
    }

    /**
     * 开启重试
     * @author anwen
     */
    public void enableRetry(Retry retry) {
        this.retry = retry;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        ExecuteMethodEnum executeMethod = invocation.getExecuteMethod();
        if (!hitLock(invocation.getExecuteMethod())) {
            return invocation.proceed();
        }
        FieldInformation fieldInformation = getVersionFieldInformation(invocation.getCollection());
        if (fieldInformation == null){
            return invocation.proceed();
        }
        boolean isUpdate = executeMethod == UPDATE || executeMethod == UPDATE_ONE || executeMethod == BULK_WRITE;
        Object result = executor(invocation,false);
        if (isUpdate) {
            if (retry != null) {
                result = beforeRetry(result,invocation);
            }
            if (updateFailException != null &&
                    getModifiedCount(result) <= 0) {
                updateFailException.fillInStackTrace();
                throw updateFailException;
            }
        }
        return result;
    }

    public Object executor(Invocation invocation, boolean autoVersion) throws Throwable {
        FieldInformation fieldInformation = getVersionFieldInformation(invocation.getCollection());
        handler(invocation,autoVersion,fieldInformation);
        return invocation.proceed();
    }

    /**
     * 乐观锁参数处理
     * @param invocation invocation
     * @author anwen
     */
    @SuppressWarnings("unchecked")
    void handler(Invocation invocation,boolean autoVersion,FieldInformation fieldInformation) {
        Object[] args = invocation.getArgs();
        ExecuteMethodEnum executeMethod = invocation.getExecuteMethod();
        if (executeMethod == SAVE){
            handleSave((List<Document>) args[0],fieldInformation);
        } else if (executeMethod == SAVE_ONE) {
            handleSave((Document) args[0],fieldInformation);
        } else if (executeMethod == UPDATE) {
            handleUpdate((List<MutablePair<Bson,Bson>>)args[0],autoVersion,fieldInformation);
        } else if (executeMethod == UPDATE_ONE) {
            handleUpdate((MutablePair<Bson,Bson>)args[0],autoVersion,fieldInformation);
        } else if (executeMethod == BULK_WRITE) {
            // 处理后的列表要写回实际执行参数，否则重建的模型不会交给驱动执行
            args[0] = handleBulkWrite((List<WriteModel<Document>>)args[0],autoVersion,fieldInformation);
        }
    }

    /**
     * 重试前的处理
     * @param result 结果
     * @param invocation invocation
     * @return {@link Object}
     * @author anwen
     */
    Object beforeRetry(Object result, Invocation invocation) throws Throwable {
        if (retry.getHitRetry() != null) {
            retry.getHitRetry().accept(new UpdateRetryResult(result, 0, invocation.getArgs(), retry));
        }
        Invocation finalInvocation;
        if (retry.getProcessIntercept()) {
            finalInvocation = new Invocation(
                    invocation.getProxy(),
                    invocation.getExecutorFactory().getOriginalExecute(),
                    invocation.getMethod(),
                    invocation.getArgs()
            );
        } else {
            finalInvocation = invocation;
        }
        if (retry.getAsyncRetry()) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return retryUpdate(result,finalInvocation);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }).get();
        }
        return retryUpdate(result, finalInvocation);
    }

    /**
     * 重试更新
     * @param result 结果
     * @param invocation invocation
     * @return {@link java.lang.Object}
     * @author anwen
     */
    Object retryUpdate(Object result,Invocation invocation) throws Throwable {
        // 锁和条件变量
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        int retryCount = 1;
        while (retryCount <= retry.getMaxRetryNum()) {
            long line = getModifiedCount(result);
            UpdateRetryResult updateRetryResult =
                    new UpdateRetryResult(result, retryCount, invocation.getArgs(), retry);
            if (line >= 1) {
                if (retry.getOnSuccess() != null) {
                    retry.getOnSuccess().accept(updateRetryResult);
                }
                return result;
            } else {
                if (retry.getOnFailure() != null) {
                    retry.getOnFailure().accept(updateRetryResult);
                }
            }
            retryCount++;
            if (retryCount > 1) {
                lock.lock();
                try {
                    // 等待动态时间
                    // 转换为纳秒
                    long remainingTimeNanos = retry.getRetryInterval() * 1_000_000L;
                    awaitCondition(condition, remainingTimeNanos);
                } finally {
                    // 确保锁释放
                    lock.unlock();
                }
            }
            result = executor(invocation,true);
        }
        if (retry.getFallback() != null) {
            return retry.getFallback().apply(
                    new UpdateRetryResult(result, retryCount,invocation.getArgs(), retry),
                    invocation
            );
        }
        return result;
    }

    /**
     * 等待执行
     * @param condition 条件变量
     * @param remainingTimeNanos 等待时间
     * @author anwen
     */
    void awaitCondition(Condition condition, long remainingTimeNanos) throws MongoPlusException {
        try {
            while (remainingTimeNanos > 0) {
                // 等待剩余时间
                remainingTimeNanos = condition.awaitNanos(remainingTimeNanos);
            }
        } catch (InterruptedException e) {
            // 恢复中断状态
            Thread.currentThread().interrupt();
            throw new OptimisticLockerException("Retry interrupted", e);
        }
    }

    /**
     * 获取更新受影响行数
     * @param result 执行结果
     * @return {@link long}
     * @author anwen
     */
    long getModifiedCount(Object result) {
        if (result instanceof UpdateResult) {
            UpdateResult updateResult = (UpdateResult) result;
            return updateResult.getModifiedCount();
        } else if (result instanceof BulkWriteResult) {
            BulkWriteResult bulkWriteResult = (BulkWriteResult) result;
            return bulkWriteResult.getModifiedCount();
        } else {
            throw new OptimisticLockerException("Unsupported result type: " + result.getClass());
        }
    }

    /**
     * 命中乐观锁
     * @param executeMethod 执行方法
     * @return {@link boolean}
     * @author anwen
     */
    boolean hitLock(ExecuteMethodEnum executeMethod) {
        return executeMethod == SAVE ||
                executeMethod == SAVE_ONE ||
                executeMethod == UPDATE ||
                executeMethod == UPDATE_ONE ||
                executeMethod == BULK_WRITE;
    }

    /**
     * save处理
     * @param documentList save参数
     * @param fieldInformation 乐观锁字段
     * @author anwen
     */
    void handleSave(List<Document> documentList, FieldInformation fieldInformation) {
        String fieldName = fieldInformation.getCamelCaseName();
        documentList.stream()
                .filter(document -> !document.containsKey(fieldName) || document.get(fieldName) == null)
                .forEach(document -> document.put(fieldName,0));
    }

    /**
     * save处理
     * @param document save参数
     * @param fieldInformation 乐观锁字段
     * @author anwen
     */
    void handleSave(Document document, FieldInformation fieldInformation) {
        String fieldName = fieldInformation.getCamelCaseName();
        if (!document.containsKey(fieldName) || document.get(fieldName) == null) {
            document.put(fieldName,0);
        }
    }

    /**
     * update处理
     * @param updatePairList update参数
     * @param fieldInformation 乐观锁字段
     * @author anwen
     */
    void handleUpdate(List<MutablePair<Bson,Bson>> updatePairList,
                      boolean autoVersion,
                      FieldInformation fieldInformation){
        if (fieldInformation == null) {
            return;
        }
        String fieldName = fieldInformation.getCamelCaseName();
        updatePairList.forEach(updatePair -> updateParamHandler(fieldName, updatePair, autoVersion));
    }

    /**
     * update处理
     * @param updatePair update参数
     * @param fieldInformation 乐观锁字段
     * @author anwen
     */
    void handleUpdate(MutablePair<Bson,Bson> updatePair,
                      boolean autoVersion,
                      FieldInformation fieldInformation){
        if (fieldInformation == null) {
            return;
        }
        String fieldName = fieldInformation.getCamelCaseName();
        updateParamHandler(fieldName, updatePair, autoVersion);
    }

    /**
     * bulkWrite处理
     * @param writeModelList bulkWrite参数，不保证可变
     * @param fieldInformation 乐观锁字段
     * @return 处理后的参数，调用方需要用它替换原参数
     * @author anwen
     */
    List<WriteModel<Document>> handleBulkWrite(List<WriteModel<Document>> writeModelList,
                                               boolean autoVersion,
                                               FieldInformation fieldInformation) {
        if (fieldInformation == null){
            return writeModelList;
        }
        String fieldName = fieldInformation.getCamelCaseName();
        // 调用方可能传入不可变列表(Collections.singletonList、unmodifiableList)，
        // 重建模型需要替换元素，所以先复制一份可变副本，不直接改调用方的列表
        List<WriteModel<Document>> handledModelList = new ArrayList<>(writeModelList);
        for (int i = 0; i < handledModelList.size(); i++) {
            WriteModel<Document> writeModel = handledModelList.get(i);
            if (writeModel instanceof InsertOneModel) {
                InsertOneModel<Document> insertOneModel = (InsertOneModel<Document>) writeModel;
                saveParamHandler(fieldName,insertOneModel.getDocument());
            }
            if (writeModel instanceof UpdateManyModel){
                UpdateManyModel<Document> updateManyModel = (UpdateManyModel<Document>) writeModel;
                if (updateManyModel.getUpdatePipeline() != null) {
                    handleUpdatePipeline(fieldName);
                    continue;
                }
                MutablePair<Bson, Bson> updatePair = MutablePair.of(updateManyModel.getFilter(),
                        updateManyModel.getUpdate());
                updateParamHandler(fieldName, updatePair, autoVersion);
                if (updatePair.getLeft() != updateManyModel.getFilter()
                        || updatePair.getRight() != updateManyModel.getUpdate()) {
                    handledModelList.set(i, new UpdateManyModel<>(updatePair.getLeft(),
                            updatePair.getRight(), updateManyModel.getOptions()));
                }
            }
        }
        return handledModelList;
    }

    /**
     * 管道更新的处理
     * <p>管道更新无法在更新语句里追加版本自增({@code $inc}只能用于普通更新文档)，补不了版本条件，
     * 所以配置了版本缺失异常时按配置拒绝执行，没有配置时保持原来的行为：跳过并告警</p>
     * @param fieldName 字段名
     * @author anwen
     */
    void handleUpdatePipeline(String fieldName) {
        if (versionIsNullException != null) {
            log.debug("The update pipeline cannot provide the optimistic lock condition," +
                    "reject it,fieldName: " + fieldName);
            versionIsNullException.fillInStackTrace();
            throw versionIsNullException;
        }
        log.warn("The update pipeline cannot be appended with the optimistic lock condition,skip it,fieldName: " + fieldName);
    }

    /**
     * save参数处理
     * @param fieldName 字段名
     * @param document document
     * @author anwen
     */
    void saveParamHandler(String fieldName, Document document){
        if (!document.containsKey(fieldName) || document.get(fieldName) == null) {
            document.put(fieldName, 0);
        }
    }

    /**
     * update参数处理
     * @param fieldName 字段名
     * @param updatePair 条件与更新值，处理完会将新的条件与更新值写回
     * @param autoVersion 是否自动版本
     * @author anwen
     */
    void updateParamHandler(String fieldName, MutablePair<Bson, Bson> updatePair, boolean autoVersion) {
        Document updateDocument = toMutableDocument(updatePair.getRight());
        Document setDocument = getDocument(updateDocument, UpdateConditionEnum.SET.getCondition());
        Integer versionValue;
        if (setDocument == null || (versionValue = getInteger(setDocument, fieldName)) == null) {
            if (!autoVersion) {
                versionValueMissing(fieldName);
                return;
            }
            Integer originalVersionValue = getInteger(toMutableDocument(updatePair.getLeft()), fieldName);
            if (originalVersionValue == null) {
                versionValueMissing(fieldName);
                return;
            }
            versionValue = originalVersionValue + (retry == null ? autoInc : retry.getAutoVersionNum());
        }
        if (setDocument != null) {
            setDocument.remove(fieldName);
        }
        Document filterDocument = toMutableDocument(updatePair.getLeft());
        filterDocument.put(fieldName, versionValue);
        Document incrementDocument = getDocument(updateDocument, INC.getCondition());
        if (incrementDocument == null) {
            incrementDocument = new Document();
        }
        incrementDocument.put(fieldName, autoInc);
        updateDocument.put(INC.getCondition(), incrementDocument);
        updatePair.setLeft(filterDocument);
        updatePair.setRight(updateDocument);
    }

    /**
     * 乐观锁原版本号未获取到时的处理
     * @param fieldName 字段名
     */
    void versionValueMissing(String fieldName) {
        log.debug("There is an optimistic lock field, but the original value " +
                "of the optimistic lock has not been obtained,fieldName: " + fieldName);
        if (versionIsNullException != null) {
            versionIsNullException.fillInStackTrace();
            throw versionIsNullException;
        }
    }

    /**
     * 将Bson转为可变的Document，已经是Document时直接返回，避免复制带来的额外开销
     * @param bson bson
     * @return {@link Document}
     */
    Document toMutableDocument(Bson bson) {
        if (bson == null) {
            return new Document();
        }
        if (bson instanceof Document) {
            return (Document) bson;
        }
        BsonDocument bsonDocument = bson.toBsonDocument(BsonDocument.class, MapCodecCache.getDefaultCodecRegistry());
        return MapCodecCache.getDefaultCodec().decode(bsonDocument.asBsonReader(), DecoderContext.builder().build());
    }

    /**
     * 获取子文档，非Document时转换为Document并写回父文档
     * @param parent 父文档
     * @param key 键
     * @return {@link Document}
     */
    Document getDocument(Document parent, String key) {
        Object value = parent.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Document) {
            return (Document) value;
        }
        if (value instanceof Map) {
            Document document = new Document();
            ((Map<?, ?>) value).forEach((k, v) -> document.put(String.valueOf(k), v));
            parent.put(key, document);
            return document;
        }
        return null;
    }

    /**
     * 获取int类型的值
     * @param document 文档
     * @param key 键
     * @return {@link Integer}
     */
    Integer getInteger(Document document, String key) {
        Object value = document.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof BsonNumber) {
            return ((BsonNumber) value).intValue();
        }
        return null;
    }

    /**
     * 获取乐观锁字段
     * @param collection 集合
     * @return {@link com.mongoplus.mapping.FieldInformation}
     * @author anwen
     */
    FieldInformation getVersionFieldInformation(MongoCollection<Document> collection){
        String fullName = collection.getNamespace().getFullName();
        Class<?> clazz;
        if (null == (clazz = MongoEntityMappingRegistry.getInstance()
                .getMappingResource(fullName))){
            return null;
        }
        return optimisticLockerExistMap.computeIfAbsent(clazz, k -> {
            TypeInformation typeInformation = TypeInformation.of(clazz);
            return typeInformation.getAnnotationField(Version.class);
        });
    }

}
