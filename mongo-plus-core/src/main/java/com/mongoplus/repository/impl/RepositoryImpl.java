package com.mongoplus.repository.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CreateIndexOptions;
import com.mongodb.client.model.DropIndexOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongoplus.aggregate.LambdaAggregateChainWrapper;
import com.mongoplus.conditions.query.LambdaQueryChainWrapper;
import com.mongoplus.conditions.update.LambdaUpdateChainWrapper;
import com.mongoplus.mapper.MongoMapperImpl;
import com.mongoplus.repository.IRepository;
import com.mongoplus.toolkit.ChainWrappers;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

/**
 * @author anwen
 */
public class RepositoryImpl<T> extends MongoMapperImpl<T> implements IRepository<T> {

    @Override
    public MongoCollection<Document> getCollection(String database) {
        return baseMapper.getMongoPlusClient().getCollection(database, clazz);
    }

    @Override
    public String createIndex(Bson bson) {
        return baseMapper.createIndex(bson, clazz);
    }

    @Override
    public String createIndex(Bson bson, IndexOptions indexOptions) {
        return baseMapper.createIndex(bson, indexOptions, clazz);
    }

    @Override
    public List<String> createIndexes(List<IndexModel> indexes) {
        return baseMapper.createIndexes(indexes, clazz);
    }

    @Override
    public List<String> createIndexes(List<IndexModel> indexes, CreateIndexOptions createIndexOptions) {
        return baseMapper.createIndexes(indexes, createIndexOptions, clazz);
    }

    @Override
    public List<Document> listIndexes() {
        return baseMapper.listIndexes(clazz);
    }

    @Override
    public void dropIndex(String indexName) {
        baseMapper.dropIndex(indexName, clazz);
    }

    @Override
    public void dropIndex(String indexName, DropIndexOptions dropIndexOptions) {
        baseMapper.dropIndex(indexName, dropIndexOptions, clazz);
    }

    @Override
    public void dropIndex(Bson keys) {
        baseMapper.dropIndex(keys, clazz);
    }

    @Override
    public void dropIndex(Bson keys, DropIndexOptions dropIndexOptions) {
        baseMapper.dropIndex(keys, dropIndexOptions, clazz);
    }

    @Override
    public void dropIndexes() {
        baseMapper.dropIndexes(clazz);
    }

    @Override
    public void dropIndexes(DropIndexOptions dropIndexOptions) {
        baseMapper.dropIndexes(dropIndexOptions, clazz);
    }

    @Override
    public LambdaQueryChainWrapper<T> lambdaQuery() {
        return ChainWrappers.lambdaQueryChain(baseMapper, clazz);
    }

    @Override
    public LambdaAggregateChainWrapper<T> lambdaAggregate() {
        return ChainWrappers.lambdaAggregatesChain(baseMapper, clazz);
    }

    @Override
    public LambdaAggregateChainWrapper<T> lambdaAggregates() {
        return ChainWrappers.lambdaAggregatesChain(baseMapper, clazz);
    }

    @Override
    public LambdaUpdateChainWrapper<T> lambdaUpdate() {
        return ChainWrappers.lambdaUpdateChain(baseMapper, clazz);
    }

}
