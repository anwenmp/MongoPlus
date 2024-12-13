package com.anwen.mongo.interceptor.business;

import com.anwen.mongo.annotation.collection.Version;
import com.anwen.mongo.domain.MongoPlusException;
import com.anwen.mongo.enums.ExecuteMethodEnum;
import com.anwen.mongo.enums.UpdateConditionEnum;
import com.anwen.mongo.execute.ExecutorFactory;
import com.anwen.mongo.interceptor.AdvancedInterceptor;
import com.anwen.mongo.interceptor.Invocation;
import com.anwen.mongo.logging.Log;
import com.anwen.mongo.logging.LogFactory;
import com.anwen.mongo.mapping.FieldInformation;
import com.anwen.mongo.mapping.TypeInformation;
import com.anwen.mongo.model.MutablePair;
import com.anwen.mongo.model.Retry;
import com.anwen.mongo.model.UpdateRetryResult;
import com.anwen.mongo.registry.MongoEntityMappingRegistry;
import com.anwen.mongo.toolkit.BsonUtil;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.anwen.mongo.enums.ExecuteMethodEnum.*;
import static com.anwen.mongo.enums.UpdateConditionEnum.INC;

/**
 * 乐观锁
 * @author anwen
 */
public class OptimisticLockerInterceptor implements AdvancedInterceptor {

    private final Log log = LogFactory.getLog(OptimisticLockerInterceptor.class);

    private final Map<Class<?>,FieldInformation> optimisticLockerExistMap = new ConcurrentHashMap<>();

    private final ExecutorFactory factory = new ExecutorFactory();

    /**
     * 字段值为空时需要抛出的异常
     */
    private RuntimeException versionIsNullException;

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
     * 设置字段值为空时，需要抛出的异常
     * @param versionIsNullException 异常
     * @author anwen
     */
    public void setVersionIsNullException(RuntimeException versionIsNullException) {
        this.versionIsNullException = versionIsNullException;
    }

    /**
     * 开启重试
     * @author anwen
     */
    public void enableRetry(Retry retry) {
        this.retry = retry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object intercept(Invocation invocation) throws Throwable {
        MongoCollection<Document> collection = invocation.getCollection();
        Object[] args = invocation.getArgs();
        ExecuteMethodEnum executeMethod = invocation.getExecuteMethod();
        if (executeMethod == SAVE){
            executeSave((List<Document>) args[0],collection);
        } else if (executeMethod == UPDATE) {
            executeUpdate((List<MutablePair<Bson,Bson>>)args[0],collection);
        } else if (executeMethod == BULK_WRITE) {
            executeBulkWrite((List<WriteModel<Document>>)args[0],collection);
        } else {
            return invocation.proceed();
        }
        Object result = invocation.proceed();
        if (retry != null) {
            boolean isUpdate = executeMethod == UPDATE || executeMethod == BULK_WRITE;
            if (isUpdate) {
                return beforeRetry(result,invocation);
            }
        }
        return result;
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
            retry.getHitRetry().accept(new UpdateRetryResult(result, 0, retry));
        }
        Invocation finalInvocation;
        if (retry.getProcessIntercept()) {
            finalInvocation = new Invocation(
                    invocation.getProxy(),
                    factory.getDefaultExecute(),
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
            });
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
            if (line >= 1) {
                if (retry.getOnSuccess() != null) {
                    retry.getOnSuccess().accept(new UpdateRetryResult(result, retryCount, retry));
                }
                return result;
            }
            retryCount++;
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
            if (retry.getOnFailure() != null) {
                retry.getOnFailure().accept(new UpdateRetryResult(result, retryCount, retry));
            }
            result = intercept(invocation);
        }
        if (retry.getFallback() != null) {
            return retry.getFallback().apply(new UpdateRetryResult(result, retryCount, retry));
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
            throw new MongoPlusException("Retry interrupted", e);
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
            throw new IllegalArgumentException("Unsupported result type: " + result.getClass());
        }
    }

    public void executeSave(List<Document> documentList, MongoCollection<Document> collection) {
        FieldInformation fieldInformation = getVersionFieldInformation(collection);
        if (fieldInformation == null){
            return;
        }
        String fieldName = fieldInformation.getCamelCaseName();
        documentList.stream()
                .filter(document -> !document.containsKey(fieldName) || document.get(fieldName) == null)
                .forEach(document -> document.put(fieldName,0));
    }

    public void executeUpdate(List<MutablePair<Bson,Bson>> updatePairList,
                                                      MongoCollection<Document> collection){
        FieldInformation fieldInformation = getVersionFieldInformation(collection);
        if (fieldInformation == null) {
            return;
        }
        String fieldName = fieldInformation.getCamelCaseName();
        Document valueDocument = new Document(INC.getCondition(), new Document(fieldName, autoInc));
        updatePairList.forEach(updatePair -> handlerUpdate(fieldName,
                updatePair.getLeft(),
                updatePair.getRight()
        ));
    }

    public void executeBulkWrite(List<WriteModel<Document>> writeModelList,
                                                       MongoCollection<Document> collection) {
        FieldInformation fieldInformation = getVersionFieldInformation(collection);
        if (fieldInformation == null){
            return;
        }
        String fieldName = fieldInformation.getCamelCaseName();
        writeModelList.forEach(writeModel -> {
            if (writeModel instanceof InsertOneModel) {
                InsertOneModel<Document> insertOneModel = (InsertOneModel<Document>) writeModel;
                handlerSave(fieldName,insertOneModel.getDocument());
            }
            if (writeModel instanceof UpdateManyModel){
                UpdateManyModel<Document> updateManyModel = (UpdateManyModel<Document>) writeModel;
                Bson filterBson = updateManyModel.getFilter();
                Bson updateBson = updateManyModel.getUpdate();
                handlerUpdate(fieldName,filterBson,updateBson);
            }
        });
    }

    void handlerSave(String fieldName, Document document){
        if (!document.containsKey(fieldName) || document.get(fieldName) == null) {
            document.put(fieldName, 0);
        }
    }

    void handlerUpdate(String fieldName,Bson filterBson,Bson updateBson) {
        Document valueDocument = new Document(INC.getCondition(), new Document(fieldName, autoInc));
        Document document = BsonUtil.asDocument(updateBson);
        Document setDocument = document.get(UpdateConditionEnum.SET.getCondition(), Document.class);
        Object versionValue = setDocument.get(fieldName);
        if (versionValue == null){
            log.debug("There is an optimistic lock field, but the original value " +
                    "of the optimistic lock has not been obtained,fieldName: "+fieldName);
            if (versionIsNullException != null) {
                throw versionIsNullException;
            }
            return;
        }
        BsonUtil.addToMap(filterBson,fieldName,versionValue);
        BsonUtil.removeFrom(setDocument,fieldName);
        BsonUtil.addAllToMap(updateBson, valueDocument);
    }

    /**
     * 获取乐观锁字段
     * @param collection 集合
     * @return {@link com.anwen.mongo.mapping.FieldInformation}
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
