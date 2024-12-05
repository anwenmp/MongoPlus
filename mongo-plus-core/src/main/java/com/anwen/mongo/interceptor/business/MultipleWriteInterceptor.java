package com.anwen.mongo.interceptor.business;

import com.anwen.mongo.cache.global.DataSourceNameCache;
import com.anwen.mongo.domain.MongoPlusDsException;
import com.anwen.mongo.enums.ExecuteMethodEnum;
import com.anwen.mongo.enums.MultipleWrite;
import com.anwen.mongo.execute.instance.DefaultExecute;
import com.anwen.mongo.handlers.write.MultipleWriteHandler;
import com.anwen.mongo.interceptor.Interceptor;
import com.anwen.mongo.logging.Log;
import com.anwen.mongo.logging.LogFactory;
import com.anwen.mongo.manager.MongoPlusClient;
import com.anwen.mongo.model.MutablePair;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.anwen.mongo.enums.MultipleWrite.*;

@SuppressWarnings("unchecked")
public class MultipleWriteInterceptor implements Interceptor {

    private final Log log = LogFactory.getLog(MultipleWriteInterceptor.class);

    private final ThreadPoolExecutor executor;

    protected final MultipleWriteHandler multipleWriteHandler;

    private final MongoPlusClient mongoPlusClient;

    private DefaultExecute execute = new DefaultExecute();

    public void setExecute(DefaultExecute execute) {
        this.execute = execute;
    }

    public MultipleWriteInterceptor(MongoPlusClient mongoPlusClient) {
        this.executor = new ThreadPoolExecutor(
                5, // 核心线程数
                20, // 最大线程数
                60L, // 空闲线程存活时间
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        this.mongoPlusClient = mongoPlusClient;
        this.multipleWriteHandler = new MultipleWriteHandler(mongoPlusClient) {};
    }

    public MultipleWriteInterceptor(MongoPlusClient mongoPlusClient, ThreadPoolExecutor executor) {
        this.mongoPlusClient = mongoPlusClient;
        this.executor = executor;
        this.multipleWriteHandler = new MultipleWriteHandler(mongoPlusClient) {};
    }

    public MultipleWriteInterceptor(MongoPlusClient mongoPlusClient, ThreadPoolExecutor executor,
                                    MultipleWriteHandler multipleWriteHandler) {
        this.mongoPlusClient = mongoPlusClient;
        this.executor = executor;
        this.multipleWriteHandler = multipleWriteHandler;
    }


    @Override
    public void afterExecute(ExecuteMethodEnum executeMethodEnum, Object[] source, Object result, MongoCollection<Document> collection) {
        if (executeMethodEnum == ExecuteMethodEnum.SAVE) {
            executeSave((List<Document>) source[0], (InsertManyOptions) source[1], collection);
        }
        if (executeMethodEnum == ExecuteMethodEnum.REMOVE) {
            executeRemove((Bson) source[0], (DeleteOptions) source[1], collection);
        }
        if (executeMethodEnum == ExecuteMethodEnum.UPDATE) {
            executeUpdate((List<MutablePair<Bson, Bson>>) source[0], (UpdateOptions) source[1], collection);
        }
        if (executeMethodEnum == ExecuteMethodEnum.BULK_WRITE) {
            executeBulkWrite((List<WriteModel<Document>>) source[0],(BulkWriteOptions) source[1], collection);
        }
    }

    public void executeSave(List<Document> documentList, InsertManyOptions options, MongoCollection<Document> collection) {
        executeMultipleWrite(
                SAVE,
                collection,
                mongoCollection -> execute.executeSave(documentList,options, mongoCollection)
        );
    }

    public void executeRemove(Bson filter, DeleteOptions options, MongoCollection<Document> collection) {
        executeMultipleWrite(
                REMOVE,
                collection,
                mongoCollection -> execute.executeRemove(filter,options, mongoCollection)
        );
    }

    public void executeUpdate(List<MutablePair<Bson, Bson>> updatePairList,
                                                       UpdateOptions options,
                                                       MongoCollection<Document> collection) {
        executeMultipleWrite(
                UPDATE,
                collection,
                mongoCollection -> execute.executeUpdate(updatePairList,options, mongoCollection)
        );
    }

    public void executeBulkWrite(List<WriteModel<Document>> writeModelList,
                                                       BulkWriteOptions options,
                                                       MongoCollection<Document> collection) {
        executeMultipleWrite(
                BULK_WRITE,
                collection,
                mongoCollection -> execute.executeBulkWrite(writeModelList,options, mongoCollection)
        );
    }

    protected void executeMultipleWrite(MultipleWrite multipleWrite,MongoCollection<Document> collection,
                                      Consumer<MongoCollection<Document>> action) {
        MongoNamespace namespace = collection.getNamespace();
        List<String> multipleWriteTargets = multipleWriteHandler.getMultipleWrite(multipleWrite, namespace);
        multipleWriteTargets.forEach(dsName -> executor.submit(() -> {
            if (!dsName.equals(DataSourceNameCache.getDataSource())) {
                MongoCollection<Document> mongoCollection = getMongoCollection(namespace, dsName);
                log.info("Executing multiple write operation on data source: " + dsName);
                action.accept(mongoCollection);
            }
        }));
    }

    protected MongoCollection<Document> getMongoCollection(MongoNamespace namespace, String dsName) {
        MongoClient mongoClient = mongoPlusClient.getMongoClient(dsName);
        if (mongoClient == null) {
            throw new MongoPlusDsException("Non-existent data source: " + dsName);
        }
        return mongoPlusClient.getCollection(dsName,namespace.getDatabaseName(),namespace.getCollectionName());
    }
}

