package com.mongoplus.index.impl;

import com.mongodb.client.model.CreateIndexOptions;
import com.mongodb.client.model.DropIndexOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongoplus.aware.MongoAwareUtils;
import com.mongoplus.aware.impl.NamespaceAware;
import com.mongoplus.execute.ExecutorFactory;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.model.MutablePair;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

/**
 * @author anwen
 */
public class DefaultBaseIndexImpl extends AbstractBaseIndex {

    private final MongoPlusClient mongoPlusClient;

    public DefaultBaseIndexImpl(MongoPlusClient mongoPlusClient, ExecutorFactory factory) {
        super(mongoPlusClient, factory);
        this.mongoPlusClient = mongoPlusClient;
    }

    @Override
    public String createIndex(Bson bson, Class<?> clazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return createIndex(namespace.left, namespace.right, bson);
    }

    @Override
    public String createIndex(Bson bson, IndexOptions indexOptions, Class<?> clazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return createIndex(namespace.left, namespace.right, bson, indexOptions);
    }

    @Override
    public List<String> createIndexes(List<IndexModel> indexes, Class<?> clazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return createIndexes(namespace.left, namespace.right, indexes);
    }

    @Override
    public List<String> createIndexes(List<IndexModel> indexes, CreateIndexOptions createIndexOptions, Class<?> clazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return createIndexes(namespace.left, namespace.right, indexes, createIndexOptions);
    }

    @Override
    public List<Document> listIndexes(Class<?> clazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        return listIndexes(namespace.left, namespace.right);
    }

    @Override
    public void dropIndex(String indexName, Class<?> clazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        dropIndex(namespace.left, namespace.right, indexName);
    }

    @Override
    public void dropIndex(String indexName, DropIndexOptions dropIndexOptions, Class<?> clazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        dropIndex(namespace.left, namespace.right, indexName, dropIndexOptions);
    }

    @Override
    public void dropIndex(Bson keys, Class<?> clazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        dropIndex(namespace.left, namespace.right, keys);
    }

    @Override
    public void dropIndex(Bson keys, DropIndexOptions dropIndexOptions, Class<?> clazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        dropIndex(namespace.left, namespace.right, keys, dropIndexOptions);
    }

    @Override
    public void dropIndexes(Class<?> clazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        dropIndexes(namespace.left, namespace.right);
    }

    @Override
    public void dropIndexes(DropIndexOptions dropIndexOptions, Class<?> clazz) {
        MutablePair<String, String> namespace = getNamespace(clazz);
        dropIndexes(namespace.left, namespace.right, dropIndexOptions);
    }

    protected MutablePair<String, String> getNamespace(Class<?> clazz) {

        String database = mongoPlusClient.getDatabase(clazz);
        String collectionName = mongoPlusClient.getCollectionName(clazz);

        // 发布感知事件
        NamespaceAware.Namespace namespace = NamespaceAware.NamespaceBuild.builder()
                .dataBase(database).collectionName(collectionName).entityClass(clazz)
                .build();
        List<NamespaceAware> handlers = MongoAwareUtils.listHandlers(NamespaceAware.class);
        for (NamespaceAware aware : handlers) {
            aware.nameSpaceAware(namespace);
        }

        return new MutablePair<>(database, collectionName);

    }

}
