package com.mongoplus.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CreateIndexOptions;
import com.mongodb.client.model.DropIndexOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongoplus.aggregate.LambdaAggregateChainWrapper;
import com.mongoplus.conditions.query.LambdaQueryChainWrapper;
import com.mongoplus.conditions.update.LambdaUpdateChainWrapper;
import com.mongoplus.mapper.MongoMapper;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

public interface IRepository<T> extends MongoMapper<T> {

    /**
     * 创建索引
     * @param bson 描述索引键的对象，该对象不能为 null
     * @return java.lang.String
     * @author JiaChaoYang
     */
    String createIndex(Bson bson);

    /**
     * 使用给定的键和选项创建索引。
     * @param bson 描述索引键的对象，该对象不能为 null
     * @param indexOptions 指数的选项
     * @return java.lang.String
     * @author JiaChaoYang
     */
    String createIndex(Bson bson, IndexOptions indexOptions);

    /**
     * 创建多个索引
     * @param indexes 索引列表
     * @return java.util.List<java.lang.String>
     * @author JiaChaoYang
     */
    List<String> createIndexes(List<IndexModel> indexes);

    /**
     * 创建多个索引
     * @param indexes 索引列表
     * @param createIndexOptions 创建索引时要使用的选项
     * @return java.util.List<java.lang.String> 索引名称列表
     * @author JiaChaoYang
     */
    List<String> createIndexes(List<IndexModel> indexes, CreateIndexOptions createIndexOptions);

    /**
     * 获取此集合中的所有索引。
     *
     * @return 列表索引可迭代接口
     */
    List<Document> listIndexes();

    /**
     * 删除给定其名称的索引。
     *
     * @param indexName 要删除的索引的名称
     */
    void dropIndex(String indexName);

    /**
     * 删除给定其名称的索引。
     *
     * @param indexName 要删除的索引的名称
     * @param dropIndexOptions 删除索引时要使用的选项
     */
    void dropIndex(String indexName, DropIndexOptions dropIndexOptions);

    /**
     * 在给定用于创建索引的键的情况下删除索引。
     *
     * @param keys 要删除的索引的键
     */
    void dropIndex(Bson keys);

    /**
     * 在给定用于创建索引的键的情况下删除索引。
     *
     * @param keys 要删除的索引的键
     * @param dropIndexOptions 删除索引时要使用的选项
     * @since 3.6
     */
    void dropIndex(Bson keys, DropIndexOptions dropIndexOptions);

    /**
     * 删除此集合上的所有索引，但 _id 上的默认值除外。
     *
     */
    void dropIndexes();

    /**
     * 删除此集合上的所有索引，但 _id 上的默认值除外。
     *
     * @param dropIndexOptions 删除索引时要使用的选项
     * @since 3.6
     */
    void dropIndexes(DropIndexOptions dropIndexOptions);

    /**
     * 获取当前service所对应的MongoCollection
     * @author JiaChaoYang
     */
    MongoCollection<Document> getCollection(String database);

    /**
     * 获取条件构造器
     * @author anwen
     */
    LambdaQueryChainWrapper<T> lambdaQuery();

    /**
     * 获取管道构造器
     * @return {@link com.mongoplus.aggregate.LambdaAggregateChainWrapper<T>}
     * @author anwen
     */
    LambdaAggregateChainWrapper<T> lambdaAggregate();

    /**
     * 获取管道构造器
     * @author anwen
     */
    LambdaAggregateChainWrapper<T> lambdaAggregates();

    /**
     * 获取修改条件构造器
     * @author anwen
     */
    LambdaUpdateChainWrapper<T> lambdaUpdate();

}
