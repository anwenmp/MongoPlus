package com.mongoplus.interceptor.business;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.UpdateResult;
import com.mongoplus.annotation.collection.Version;
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
import com.mongoplus.toolkit.BsonUtil;
import org.bson.Document;
import org.bson.conversions.Bson;

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
            handleBulkWrite((List<WriteModel<Document>>)args[0],autoVersion,fieldInformation);
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
        return executeMethod == SAVE || executeMethod == UPDATE || executeMethod == BULK_WRITE;
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
        updatePairList.forEach(updatePair -> updateParamHandler(fieldName,
                updatePair.getLeft(),
                updatePair.getRight(),
                autoVersion
        ));
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
        updateParamHandler(fieldName,
                updatePair.getLeft(),
                updatePair.getRight(),
                autoVersion
        );
    }

    /**
     * bulkWrite处理
     * @param writeModelList bulkWrite参数
     * @param fieldInformation 乐观锁字段
     * @author anwen
     */
    void handleBulkWrite(List<WriteModel<Document>> writeModelList,
                         boolean autoVersion,
                         FieldInformation fieldInformation) {
        if (fieldInformation == null){
            return;
        }
        String fieldName = fieldInformation.getCamelCaseName();
        writeModelList.forEach(writeModel -> {
            if (writeModel instanceof InsertOneModel) {
                InsertOneModel<Document> insertOneModel = (InsertOneModel<Document>) writeModel;
                saveParamHandler(fieldName,insertOneModel.getDocument());
            }
            if (writeModel instanceof UpdateManyModel){
                UpdateManyModel<Document> updateManyModel = (UpdateManyModel<Document>) writeModel;
                Bson filterBson = updateManyModel.getFilter();
                Bson updateBson = updateManyModel.getUpdate();
                updateParamHandler(fieldName,filterBson,updateBson,autoVersion);
            }
        });
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
     * @param filterBson 条件
     * @param updateBson 更新值
     * @author anwen
     */
    void updateParamHandler(String fieldName, Bson filterBson, Bson updateBson, boolean autoVersion) {
        Document valueDocument = new Document(INC.getCondition(), new Document(fieldName, autoInc));
        Document document = BsonUtil.asDocument(updateBson);
        Document setDocument = document.get(UpdateConditionEnum.SET.getCondition(), Document.class);
        Integer versionValue;
        if (setDocument == null || (versionValue = setDocument.getInteger(fieldName)) == null) {
            if (!autoVersion) {
                log.debug("There is an optimistic lock field, but the original value " +
                        "of the optimistic lock has not been obtained,fieldName: " + fieldName);
                if (versionIsNullException != null) {
                    versionIsNullException.fillInStackTrace();
                    throw versionIsNullException;
                }
                return;
            }
            Document filterDocument = BsonUtil.asDocument(filterBson);
            Integer originalVersionValue = filterDocument.getInteger(fieldName);
            versionValue = originalVersionValue+ retry.getAutoVersionNum();
        }
        BsonUtil.addToMap(filterBson,fieldName,versionValue);
        BsonUtil.removeFrom(setDocument,fieldName);
        BsonUtil.addAllToMap(updateBson, valueDocument);
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
