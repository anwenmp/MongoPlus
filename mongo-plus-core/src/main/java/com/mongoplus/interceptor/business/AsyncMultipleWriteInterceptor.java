package com.mongoplus.interceptor.business;

import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.domain.MongoPlusDsException;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.enums.MultipleWrite;
import com.mongoplus.execute.instance.DefaultExecute;
import com.mongoplus.handlers.write.MultipleWriteHandler;
import com.mongoplus.interceptor.AdvancedInterceptor;
import com.mongoplus.interceptor.Invocation;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.logic.LogicRemove;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.model.MutablePair;
import com.mongoplus.toolkit.BsonUtil;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.mongoplus.constant.SqlOperationConstant._ID;
import static com.mongoplus.enums.MultipleWrite.*;

/**
 * 异步多写，并不能保证事务，通常用于数据备份
 * @author anwen
 */
@SuppressWarnings("unchecked")
public class AsyncMultipleWriteInterceptor implements AdvancedInterceptor {

    private final Log log = LogFactory.getLog(AsyncMultipleWriteInterceptor.class);

    private final ThreadPoolExecutor executor;

    protected MultipleWriteHandler multipleWriteHandler;

    private final MongoPlusClient mongoPlusClient;

    private DefaultExecute execute = new DefaultExecute();

    public void setExecute(DefaultExecute execute) {
        this.execute = execute;
    }

    public void setMultipleWriteHandler(MultipleWriteHandler multipleWriteHandler) {
        this.multipleWriteHandler = multipleWriteHandler;
    }

    public AsyncMultipleWriteInterceptor(MongoPlusClient mongoPlusClient) {
        this.executor = defaultExecutor();
        this.mongoPlusClient = mongoPlusClient;
        this.multipleWriteHandler = new MultipleWriteHandler(mongoPlusClient) {};
    }

    public AsyncMultipleWriteInterceptor(MongoPlusClient mongoPlusClient,MultipleWriteHandler multipleWriteHandler) {
        this.executor = defaultExecutor();
        this.mongoPlusClient = mongoPlusClient;
        this.multipleWriteHandler = multipleWriteHandler;
    }

    public AsyncMultipleWriteInterceptor(MongoPlusClient mongoPlusClient, ThreadPoolExecutor executor) {
        this.mongoPlusClient = mongoPlusClient;
        this.executor = executor;
        this.multipleWriteHandler = new MultipleWriteHandler(mongoPlusClient) {};
    }

    public AsyncMultipleWriteInterceptor(MongoPlusClient mongoPlusClient, ThreadPoolExecutor executor,
                                         MultipleWriteHandler multipleWriteHandler) {
        this.mongoPlusClient = mongoPlusClient;
        this.executor = executor;
        this.multipleWriteHandler = multipleWriteHandler;
    }

    final ThreadPoolExecutor defaultExecutor(){
        return new ThreadPoolExecutor(
                5, // 核心线程数
                20, // 最大线程数
                60L, // 空闲线程存活时间
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        ExecuteMethodEnum executeMethod = invocation.getExecuteMethod();
        Object[] source = invocation.getArgs();
        MongoCollection<Document> collection = invocation.getCollection();
        if (executeMethod == ExecuteMethodEnum.SAVE) {
            executeSave((List<Document>) source[0], (InsertManyOptions) source[1], collection);
        }
        if (executeMethod == ExecuteMethodEnum.SAVE_ONE) {
            executeSave((Document) source[0], (InsertOneOptions) source[1], collection);
        }
        if (executeMethod == ExecuteMethodEnum.REMOVE || executeMethod == ExecuteMethodEnum.REMOVE_ONE) {
            executeRemove((Bson) source[0], (DeleteOptions) source[1],invocation, collection);
        }
        if (executeMethod == ExecuteMethodEnum.UPDATE) {
            executeUpdate((List<MutablePair<Bson, Bson>>) source[0], (UpdateOptions) source[1], collection);
        }
        if (executeMethod == ExecuteMethodEnum.UPDATE_ONE) {
            executeUpdate((MutablePair<Bson, Bson>) source[0], (UpdateOptions) source[1], collection);
        }
        if (executeMethod == ExecuteMethodEnum.REPLACE) {
            BsonDocument filter = shareReplacementFilter(source);
            UpdateResult result = (UpdateResult) invocation.proceed();
            // 主库命中后再复制，使用高级拦截器处理过的版本条件和替换文档。
            if (result.wasAcknowledged()) {
                BsonValue upsertedId = result.getUpsertedId();
                if (result.getMatchedCount() > 0 || upsertedId != null) {
                    Document replacement = (Document) source[1];
                    if (upsertedId != null) {
                        replacement = new Document(replacement).append(_ID, upsertedId);
                    }
                    executeReplace(filter, replacement, (ReplaceOptions) source[2], collection);
                }
            }
            return result;
        }
        if (executeMethod == ExecuteMethodEnum.FIND_ONE_AND_REPLACE) {
            BsonDocument filter = shareReplacementFilter(source);
            Document result = (Document) invocation.proceed();
            FindOneAndReplaceOptions options = (FindOneAndReplaceOptions) source[2];
            if (result != null && (options == null || !options.isUpsert() || result.containsKey(_ID))) {
                Document replacement = (Document) source[1];
                Bson target = filter;
                if (result.containsKey(_ID)) {
                    Object id = result.get(_ID);
                    target = Filters.and(filter, Filters.eq(_ID, id));
                    if (options != null && options.isUpsert()) {
                        replacement = new Document(replacement).append(_ID, id);
                    }
                }
                executeFindOneAndReplace(target, replacement, options, collection);
            }
            return result;
        }
        if (executeMethod == ExecuteMethodEnum.BULK_WRITE) {
            executeBulkWrite((List<WriteModel<Document>>) source[0],(BulkWriteOptions) source[1], collection);
        }
        return invocation.proceed();
    }

    private BsonDocument shareReplacementFilter(Object[] source) {
        // 反射代理各有自己的参数数组，使用可变 BSON 共享内层乐观锁的条件改动。
        BsonDocument filter = BsonUtil.asBsonDocument((Bson) source[0]).clone();
        source[0] = filter;
        return filter;
    }

    void executeSave(List<Document> documentList, InsertManyOptions options, MongoCollection<Document> collection) {
        executeMultipleWrite(
                SAVE,
                collection,
                mongoCollection -> execute.executeSave(documentList,options, mongoCollection)
        );
    }

    void executeSave(Document document, InsertOneOptions options, MongoCollection<Document> collection) {
        executeMultipleWrite(
                SAVE,
                collection,
                mongoCollection -> execute.executeSaveOne(document,options, mongoCollection)
        );
    }

    void executeRemove(Bson filter, DeleteOptions options,Invocation invocation,
                              MongoCollection<Document> collection) {
        executeMultipleWrite(
                REMOVE,
                collection,
                mongoCollection -> {
                    try {
                        LogicRemove.logic(invocation,mongoCollection);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    void executeUpdate(List<MutablePair<Bson, Bson>> updatePairList,
                                                       UpdateOptions options,
                                                       MongoCollection<Document> collection) {
        executeMultipleWrite(
                UPDATE,
                collection,
                mongoCollection -> execute.executeUpdate(updatePairList,options, mongoCollection)
        );
    }

    void executeUpdate(MutablePair<Bson, Bson> updatePair,
                       UpdateOptions options,
                       MongoCollection<Document> collection) {
        executeMultipleWrite(
                UPDATE,
                collection,
                mongoCollection -> execute.executeUpdateOne(updatePair,options, mongoCollection)
        );
    }

    void executeReplace(Bson filter, Document replacement, ReplaceOptions options, MongoCollection<Document> collection) {
        executeMultipleWrite(
                UPDATE,
                collection,
                mongoCollection -> execute.executeReplace(filter, replacement, options, mongoCollection)
        );
    }

    void executeFindOneAndReplace(Bson filter, Document replacement, FindOneAndReplaceOptions options,
                                  MongoCollection<Document> collection) {
        executeMultipleWrite(
                UPDATE,
                collection,
                mongoCollection -> execute.executeFindOneAndReplace(filter, replacement, options, mongoCollection)
        );
    }

    void executeBulkWrite(List<WriteModel<Document>> writeModelList,
                                                       BulkWriteOptions options,
                                                       MongoCollection<Document> collection) {
        executeMultipleWrite(
                BULK_WRITE,
                collection,
                mongoCollection -> execute.executeBulkWrite(writeModelList,options, mongoCollection)
        );
    }

    void executeMultipleWrite(MultipleWrite multipleWrite,MongoCollection<Document> collection,
                                      Consumer<MongoCollection<Document>> action) {
        MongoNamespace namespace = collection.getNamespace();
        String sourceDataSource = DataSourceNameCache.getDataSource();
        List<String> multipleWriteTargets = multipleWriteHandler.getMultipleWrite(multipleWrite, namespace);
        multipleWriteTargets.stream().filter(dsName -> !dsName.equals(sourceDataSource))
            .forEach(dsName -> executor.submit(() -> {
                MongoCollection<Document> mongoCollection = getMongoCollection(namespace, dsName);
                log.info("Executing multiple write operation on data source: " + dsName);
                action.accept(mongoCollection);
            }));
    }

    MongoCollection<Document> getMongoCollection(MongoNamespace namespace, String dsName) {
        MongoClient mongoClient = mongoPlusClient.getMongoClient(dsName);
        if (mongoClient == null) {
            throw new MongoPlusDsException("Non-existent data source: " + dsName);
        }
        return mongoPlusClient.getCollection(dsName,namespace.getDatabaseName(),namespace.getCollectionName());
    }
}

