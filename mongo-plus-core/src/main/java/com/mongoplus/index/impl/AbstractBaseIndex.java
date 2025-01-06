package com.mongoplus.index.impl;

import com.mongodb.client.model.CreateIndexOptions;
import com.mongodb.client.model.DropIndexOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongoplus.execute.ExecutorFactory;
import com.mongoplus.index.BaseIndex;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.mapper.AbstractBaseMapper;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

/**
 * @author anwen
 */
public abstract class AbstractBaseIndex implements BaseIndex {

    private final Log log = LogFactory.getLog(AbstractBaseMapper.class);

    private final MongoPlusClient mongoPlusClient;

    private final ExecutorFactory factory;

    public AbstractBaseIndex(MongoPlusClient mongoPlusClient, ExecutorFactory factory) {
        this.mongoPlusClient = mongoPlusClient;
        this.factory = factory;
    }

    @Override
    public String createIndex(String database, String collectionName, Bson bson) {
        return factory.getExecute().doCreateIndex(bson, mongoPlusClient.getCollection(database, collectionName));
    }

    @Override
    public String createIndex(String database, String collectionName, Bson bson, IndexOptions indexOptions) {
        return factory.getExecute().doCreateIndex(
                bson,
                indexOptions, mongoPlusClient.getCollection(database, collectionName)
        );
    }

    @Override
    public List<String> createIndexes(String database, String collectionName, List<IndexModel> indexes) {
        return factory.getExecute().doCreateIndexes(indexes, mongoPlusClient.getCollection(database, collectionName));
    }

    @Override
    public List<String> createIndexes(String database, String collectionName, List<IndexModel> indexes,
                                      CreateIndexOptions createIndexOptions) {
        return factory.getExecute().doCreateIndexes(indexes, createIndexOptions,
                mongoPlusClient.getCollection(database, collectionName)
        );
    }

    @Override
    public List<Document> listIndexes(String database, String collectionName) {
        return factory.getExecute().doListIndexes(mongoPlusClient.getCollection(database, collectionName));
    }

    @Override
    public void dropIndex(String database, String collectionName, String indexName) {
        factory.getExecute().doDropIndex(indexName, mongoPlusClient.getCollection(database, collectionName));
    }

    @Override
    public void dropIndex(String database, String collectionName, String indexName,
                          DropIndexOptions dropIndexOptions) {
        factory.getExecute().doDropIndex(indexName, dropIndexOptions,
                mongoPlusClient.getCollection(database, collectionName));
    }

    @Override
    public void dropIndex(String database, String collectionName, Bson keys) {
        factory.getExecute().doDropIndex(keys, mongoPlusClient.getCollection(database, collectionName));
    }

    @Override
    public void dropIndex(String database, String collectionName, Bson keys, DropIndexOptions dropIndexOptions) {
        factory.getExecute().doDropIndex(keys, dropIndexOptions, mongoPlusClient.getCollection(database, collectionName));
    }

    @Override
    public void dropIndexes(String database, String collectionName) {
        factory.getExecute().doDropIndexes(mongoPlusClient.getCollection(database, collectionName));
    }

    @Override
    public void dropIndexes(String database, String collectionName, DropIndexOptions dropIndexOptions) {
        factory.getExecute().doDropIndexes(dropIndexOptions, mongoPlusClient.getCollection(database, collectionName));
    }

}
