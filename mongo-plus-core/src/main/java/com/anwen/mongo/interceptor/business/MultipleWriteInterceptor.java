package com.anwen.mongo.interceptor.business;

import com.anwen.mongo.domain.MongoPlusDsException;
import com.anwen.mongo.enums.MultipleWrite;
import com.anwen.mongo.execute.instance.DefaultExecute;
import com.anwen.mongo.handlers.write.MultipleWriteHandler;
import com.anwen.mongo.interceptor.Interceptor;
import com.anwen.mongo.manager.MongoPlusClient;
import com.anwen.mongo.model.MutablePair;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.anwen.mongo.enums.MultipleWrite.*;

public class MultipleWriteInterceptor implements Interceptor {

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
    public List<Document> executeSave(List<Document> documentList, MongoCollection<Document> collection) {
        List<Document> finalDocumentList = Interceptor.super.executeSave(documentList, collection);
        executeMultipleWrite(
                SAVE,
                collection,
                mongoCollection -> execute.executeSave(finalDocumentList, mongoCollection)
        );
        return finalDocumentList;
    }

    @Override
    public Bson executeRemove(Bson filter, MongoCollection<Document> collection) {
        Bson finalFilter = Interceptor.super.executeRemove(filter, collection);
        executeMultipleWrite(
                REMOVE,
                collection,
                mongoCollection -> execute.executeRemove(finalFilter, mongoCollection)
        );
        return finalFilter;
    }

    @Override
    public List<MutablePair<Bson, Bson>> executeUpdate(List<MutablePair<Bson, Bson>> updatePairList,
                                                       MongoCollection<Document> collection) {
        List<MutablePair<Bson, Bson>> finalUpdatePairList = Interceptor.super.executeUpdate(updatePairList, collection);
        executeMultipleWrite(
                UPDATE,
                collection,
                mongoCollection -> execute.executeUpdate(finalUpdatePairList, mongoCollection)
        );
        return finalUpdatePairList;
    }

    @Override
    public List<WriteModel<Document>> executeBulkWrite(List<WriteModel<Document>> writeModelList,
                                                       MongoCollection<Document> collection) {
        List<WriteModel<Document>> finalWriteModelList = Interceptor.super.executeBulkWrite(writeModelList, collection);
        executeMultipleWrite(
                BULK_WRITE,
                collection,
                mongoCollection -> execute.executeBulkWrite(finalWriteModelList, mongoCollection)
        );
        return finalWriteModelList;
    }

    private void executeMultipleWrite(MultipleWrite multipleWrite,MongoCollection<Document> collection,
                                      Consumer<MongoCollection<Document>> action) {
        MongoNamespace namespace = collection.getNamespace();
        List<String> multipleWriteTargets = multipleWriteHandler.getMultipleWrite(multipleWrite, namespace);

        multipleWriteTargets.forEach(dsName -> executor.submit(() -> {
            MongoCollection<Document> mongoCollection = getMongoCollection(namespace, dsName);
            action.accept(mongoCollection);
        }));
    }

    private MongoCollection<Document> getMongoCollection(MongoNamespace namespace, String dsName) {
        MongoClient mongoClient = mongoPlusClient.getMongoClient(dsName);
        if (mongoClient == null) {
            throw new MongoPlusDsException("Non-existent data source: " + dsName);
        }
        return mongoPlusClient.getCollection(
                namespace.getDatabaseName(),
                namespace.getCollectionName()
        );
    }
}

