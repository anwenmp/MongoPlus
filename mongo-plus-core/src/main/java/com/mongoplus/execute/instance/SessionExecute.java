package com.mongoplus.execute.instance;

import com.mongodb.BasicDBObject;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import com.mongoplus.context.MongoTransactionContext;
import com.mongoplus.context.MongoTransactionStatus;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongoplus.convert.DocumentMapperConvert;
import com.mongoplus.execute.Execute;
import com.mongoplus.model.MutablePair;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * session实例
 *
 * @author JiaChaoYang
 **/
public class SessionExecute implements Execute {

    private final ClientSession clientSession;

    private final MongoTransactionStatus transactionStatus;

    public SessionExecute(ClientSession clientSession) {
        this.clientSession = clientSession;
        MongoTransactionStatus current = MongoTransactionContext.getMongoTransactionStatus();
        this.transactionStatus = current != null && current.getClientSession() == clientSession ? current : null;
    }

    private MongoCollection<Document> transactionCollection(MongoCollection<Document> collection) {
        return transactionStatus != null && transactionStatus.hasTimeout()
                ? TransactionTimeoutProxy.wrap(collection, transactionStatus) : collection;
    }

    @Override
    public InsertOneResult executeSaveOne(Document document, InsertOneOptions options, MongoCollection<Document> collection) {
        return Optional.ofNullable(options)
                .map(o -> transactionCollection(collection).insertOne(clientSession,document,o))
                .orElseGet(() -> transactionCollection(collection).insertOne(clientSession,document));
    }

    @Override
    public InsertManyResult executeSave(List<Document> documentList, InsertManyOptions options,
                                        MongoCollection<Document> collection) {
        return Optional.ofNullable(options)
                .map(o -> transactionCollection(collection).insertMany(clientSession,documentList,o))
                .orElseGet(() -> transactionCollection(collection).insertMany(clientSession,documentList));
    }

    @Override
    public DeleteResult executeRemoveOne(Bson filter, DeleteOptions options, MongoCollection<Document> collection) {
        return Optional.ofNullable(options)
                .map(o -> transactionCollection(collection).deleteOne(clientSession,filter,o))
                .orElseGet(() -> transactionCollection(collection).deleteOne(clientSession,filter));
    }

    @Override
    public Document executeFindOneAndRemove(Bson filter, FindOneAndDeleteOptions options,
                                            MongoCollection<Document> collection) {
        // 未命中时返回 null，不能用 Optional.map 的空结果触发第二次删除。
        if (options == null) {
            return transactionCollection(collection).findOneAndDelete(clientSession, filter);
        }
        return transactionCollection(collection).findOneAndDelete(clientSession, filter, options);
    }

    @Override
    public BulkWriteResult executeBulkWrite(List<WriteModel<Document>> writeModelList,
                                            BulkWriteOptions options,
                                            MongoCollection<Document> collection) {
        return Optional.ofNullable(options)
                .map(o -> transactionCollection(collection).bulkWrite(clientSession,writeModelList,o))
                .orElseGet(() -> transactionCollection(collection).bulkWrite(clientSession,writeModelList));
    }

    @Override
    public DeleteResult executeRemove(Bson filter, DeleteOptions options,
                                      MongoCollection<Document> collection) {
        return Optional.ofNullable(options)
                .map(o -> transactionCollection(collection).deleteMany(clientSession,filter,o))
                .orElseGet(() -> transactionCollection(collection).deleteMany(clientSession,filter));
    }

    @Override
    public UpdateResult executeUpdateOne(MutablePair<Bson, Bson> bsonPair, UpdateOptions options, MongoCollection<Document> collection) {
        return Optional.ofNullable(options)
                .map(o -> transactionCollection(collection).updateOne(clientSession,bsonPair.getLeft(), bsonPair.getRight(), o))
                .orElseGet(() -> transactionCollection(collection).updateOne(clientSession,bsonPair.getLeft(), bsonPair.getRight()));
    }

    @Override
    public <T> FindIterable<T> executeQuery(Bson queryBasic, BasicDBObject projectionList,
                                            BasicDBObject sortCond,
                                            Class<T> clazz,
                                            MongoCollection<Document> collection) {
        return Optional.ofNullable(queryBasic)
                .map(qb -> transactionCollection(collection).find(clientSession,qb,clazz))
                .orElseGet(() -> transactionCollection(collection).find(clientSession,clazz))
                .projection(projectionList)
                .sort(sortCond);
    }

    @Override
    public <T> AggregateIterable<T> executeAggregate(List<? extends Bson> aggregateConditionList,
                                                     Class<T> clazz,
                                                     MongoCollection<Document> collection) {
        return transactionCollection(collection).aggregate(clientSession,aggregateConditionList,clazz);
    }

    @Override
    public long executeCount(BasicDBObject queryBasic, CountOptions countOptions,
                             MongoCollection<Document> collection) {
        return Optional.ofNullable(countOptions)
                .map(co -> transactionCollection(collection).countDocuments(clientSession, queryBasic,co))
                .orElseGet(() -> transactionCollection(collection).countDocuments(clientSession, queryBasic));
    }

    @Override
    public long estimatedDocumentCount(MongoCollection<Document> collection) {
        return transactionCollection(collection).countDocuments(clientSession);
    }

    @Override
    public UpdateResult executeUpdate(List<MutablePair<Bson, Bson>> bsonPairList,
                                      UpdateOptions options,
                                      MongoCollection<Document> collection) {
        AtomicReference<Long> matchedCount = new AtomicReference<>(0L);
        AtomicReference<Long> modifiedCount = new AtomicReference<>(0L);
        AtomicReference<BsonValue> upstartedId = new AtomicReference<>();
        bsonPairList.forEach(bsonPair -> {
            UpdateResult updateResult = Optional.ofNullable(options)
                    .map(o -> transactionCollection(collection).updateMany(clientSession,bsonPair.getLeft(), bsonPair.getRight(), o))
                    .orElseGet(() -> transactionCollection(collection).updateMany(clientSession,bsonPair.getLeft(), bsonPair.getRight()));
            matchedCount.updateAndGet(v -> v + updateResult.getMatchedCount());
            modifiedCount.updateAndGet(v -> v + updateResult.getModifiedCount());
            upstartedId.set(updateResult.getUpsertedId());
        });
        return UpdateResult.acknowledged(matchedCount.get(), modifiedCount.get(), upstartedId.get());
    }

    @Override
    public String doCreateIndex(Bson bson, MongoCollection<Document> collection) {
        return transactionCollection(collection).createIndex(clientSession,bson);
    }

    @Override
    public String doCreateIndex(Bson bson, IndexOptions indexOptions, MongoCollection<Document> collection) {
        return transactionCollection(collection).createIndex(clientSession,bson,indexOptions);
    }

    @Override
    public List<String> doCreateIndexes(List<IndexModel> indexes, MongoCollection<Document> collection) {
        return transactionCollection(collection).createIndexes(clientSession,indexes);
    }

    @Override
    public List<String> doCreateIndexes(List<IndexModel> indexes, CreateIndexOptions createIndexOptions,
                                        MongoCollection<Document> collection) {
        return transactionCollection(collection).createIndexes(clientSession,indexes,createIndexOptions);
    }

    @Override
    public List<Document> doListIndexes(MongoCollection<Document> collection) {
        return DocumentMapperConvert.indexesIterableToDocument(transactionCollection(collection).listIndexes(clientSession));
    }

    @Override
    public void doDropIndex(String indexName, MongoCollection<Document> collection) {
        transactionCollection(collection).dropIndex(clientSession,indexName);
    }

    @Override
    public void doDropIndex(String indexName, DropIndexOptions dropIndexOptions,
                            MongoCollection<Document> collection) {
        transactionCollection(collection).dropIndex(clientSession,indexName,dropIndexOptions);
    }

    @Override
    public void doDropIndex(Bson keys, MongoCollection<Document> collection) {
        transactionCollection(collection).dropIndex(clientSession,keys);
    }

    @Override
    public void doDropIndex(Bson keys, DropIndexOptions dropIndexOptions,
                            MongoCollection<Document> collection) {
        transactionCollection(collection).dropIndex(clientSession,keys,dropIndexOptions);
    }

    @Override
    public void doDropIndexes(MongoCollection<Document> collection) {
        transactionCollection(collection).dropIndexes(clientSession);
    }

    @Override
    public void doDropIndexes(DropIndexOptions dropIndexOptions,
                              MongoCollection<Document> collection) {
        transactionCollection(collection).dropIndexes(clientSession,dropIndexOptions);
    }
}
