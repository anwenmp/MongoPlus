package com.mongoplus.execute;

import com.mongodb.BasicDBObject;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongoplus.annotation.comm.Nullable;
import com.mongoplus.model.MutablePair;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

/**
 * 执行器接口
 *
 * @author JiaChaoYang
 **/
public interface Execute {

    /**
     * 添加执行,执行的是insertOne
     * @param document 需要添加的Document
     * @param collection 集合
     * @return {@link com.mongodb.client.result.InsertOneResult}
     * @author anwen
     */
    InsertOneResult executeSaveOne(Document document, @Nullable InsertOneOptions options,
                                   MongoCollection<Document> collection);

    /**
     * 添加执行
     * @param documentList 需要添加的Document
     * @param collection 集合
     * @return {@link InsertManyResult}
     * @author anwen
     */
    InsertManyResult executeSave(List<Document> documentList, @Nullable InsertManyOptions options,
                                 MongoCollection<Document> collection);

    /**
     * 删除执行 执行的是deleteOne
     * @param filter 删除条件
     * @param collection 集合
     * @return {@link com.mongodb.client.result.DeleteResult}
     * @author anwen
     */
    DeleteResult executeRemoveOne(Bson filter, @Nullable DeleteOptions options, MongoCollection<Document> collection);

    /**
     * 删除执行
     * @param filter 删除条件
     * @param collection 集合
     * @return {@link com.mongodb.client.result.DeleteResult}
     * @author anwen
     */
    DeleteResult executeRemove(Bson filter, @Nullable DeleteOptions options, MongoCollection<Document> collection);

    /**
     * 更新执行,执行的是updateOne
     * @param bsonPair 更新条件
     * @param collection 集合
     * @return {@link com.mongodb.client.result.UpdateResult}
     * @author anwen
     */
    UpdateResult executeUpdateOne(MutablePair<Bson,Bson> bsonPair, @Nullable UpdateOptions options,
                                  MongoCollection<Document> collection);

    /**
     * 更新执行
     * @param bsonPairList 更新条件
     * @param collection 集合
     * @return {@link com.mongodb.client.result.UpdateResult}
     * @author anwen
     */
    UpdateResult executeUpdate(List<MutablePair<Bson,Bson>> bsonPairList, @Nullable UpdateOptions options,
                               MongoCollection<Document> collection);

    /**
     * 查询执行
     * @param queryBasic 查询条件
     * @param projectionList project
     * @param sortCond 排序
     * @param clazz 返回体类型
     * @param collection 集合
     * @return {@link com.mongodb.client.FindIterable<T>}
     * @author anwen
     */
    <T> FindIterable<T> executeQuery(Bson queryBasic, BasicDBObject projectionList, BasicDBObject sortCond,
                                     Class<T> clazz,
                                     MongoCollection<Document> collection);

    /**
     * 管道执行
     * @param aggregateConditionList 管道条件
     * @param clazz 返回体类型
     * @param collection 集合
     * @return {@link com.mongodb.client.AggregateIterable<T>}
     * @author anwen
     */
    <T> AggregateIterable<T> executeAggregate(List<? extends Bson> aggregateConditionList, Class<T> clazz,
                                              MongoCollection<Document> collection);

    /**
     * 统计执行
     * @param queryBasic 查询条件
     * @param countOptions 统计选项
     * @param collection 集合
     * @return {@link long}
     * @author anwen
     */
    long executeCount(BasicDBObject queryBasic,@Nullable CountOptions countOptions,MongoCollection<Document> collection);

    /**
     * 不接受任何条件的统计
     * @param collection 集合
     * @return {@link long}
     * @author anwen
     */
    long estimatedDocumentCount(MongoCollection<Document> collection);

    /**
     * 写入多个执行
     * @param writeModelList 写入实体集合
     * @param collection 集合
     * @return {@link com.mongodb.bulk.BulkWriteResult}
     * @author anwen
     */
    BulkWriteResult executeBulkWrite(List<WriteModel<Document>> writeModelList,@Nullable BulkWriteOptions options,
                                     MongoCollection<Document> collection);

    String doCreateIndex(Bson bson,MongoCollection<Document> collection);

    String doCreateIndex(Bson bson,IndexOptions indexOptions,MongoCollection<Document> collection);

    List<String> doCreateIndexes(List<IndexModel> indexes,MongoCollection<Document> collection);

    List<String> doCreateIndexes(List<IndexModel> indexes, CreateIndexOptions createIndexOptions,
                                 MongoCollection<Document> collection);

    List<Document> doListIndexes(MongoCollection<Document> collection);

    void doDropIndex(String indexName,MongoCollection<Document> collection);

    void doDropIndex(String indexName,DropIndexOptions dropIndexOptions,MongoCollection<Document> collection);

    void doDropIndex(Bson keys,MongoCollection<Document> collection);

    void doDropIndex(Bson keys,DropIndexOptions dropIndexOptions,MongoCollection<Document> collection);

    void doDropIndexes(MongoCollection<Document> collection);

    void doDropIndexes(DropIndexOptions dropIndexOptions,MongoCollection<Document> collection);

}
