package com.mongoplus.execute;

import com.mongodb.BasicDBObject;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.UpdateResult;
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
     * 添加执行
     * @param documentList 需要添加的Document
     * @param collection 集合
     * @return {@link InsertManyResult}
     * @author anwen
     */
    default InsertManyResult executeSave(List<Document> documentList, MongoCollection<Document> collection){
        return executeSave(documentList, null, collection);
    }

    /**
     * 添加执行
     * @param documentList 需要添加的Document
     * @param collection 集合
     * @return {@link InsertManyResult}
     * @author anwen
     */
    InsertManyResult executeSave(List<Document> documentList, InsertManyOptions options,
                                 MongoCollection<Document> collection);

    /**
     * 删除执行
     * @param filter 删除条件
     * @param collection 集合
     * @return {@link com.mongodb.client.result.DeleteResult}
     * @author anwen
     */
    default DeleteResult executeRemove(Bson filter, MongoCollection<Document> collection){
        return executeRemove(filter,null,collection);
    }

    /**
     * 删除执行
     * @param filter 删除条件
     * @param collection 集合
     * @return {@link com.mongodb.client.result.DeleteResult}
     * @author anwen
     */
    DeleteResult executeRemove(Bson filter, DeleteOptions options, MongoCollection<Document> collection);

    /**
     * 更新执行
     * @param bsonPairList 更新条件
     * @param collection 集合
     * @return {@link com.mongodb.client.result.UpdateResult}
     * @author anwen
     */
    default UpdateResult executeUpdate(List<MutablePair<Bson,Bson>> bsonPairList,
                                       MongoCollection<Document> collection){
        return executeUpdate(bsonPairList,null,collection);
    }

    /**
     * 更新执行
     * @param bsonPairList 更新条件
     * @param collection 集合
     * @return {@link com.mongodb.client.result.UpdateResult}
     * @author anwen
     */
    UpdateResult executeUpdate(List<MutablePair<Bson,Bson>> bsonPairList, UpdateOptions options,
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
    long executeCount(BasicDBObject queryBasic,CountOptions countOptions,MongoCollection<Document> collection);

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
    default BulkWriteResult executeBulkWrite(List<WriteModel<Document>> writeModelList,
                                             MongoCollection<Document> collection){
        return executeBulkWrite(writeModelList,null,collection);
    }

    /**
     * 写入多个执行
     * @param writeModelList 写入实体集合
     * @param collection 集合
     * @return {@link com.mongodb.bulk.BulkWriteResult}
     * @author anwen
     */
    BulkWriteResult executeBulkWrite(List<WriteModel<Document>> writeModelList,BulkWriteOptions options,
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
