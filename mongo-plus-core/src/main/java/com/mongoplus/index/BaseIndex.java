package com.mongoplus.index;

import com.mongodb.client.model.CreateIndexOptions;
import com.mongodb.client.model.DropIndexOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

/**
 * @author anwen
 */
public interface BaseIndex extends Index {

    String createIndex(Bson bson, Class<?> clazz);

    String createIndex(Bson bson, IndexOptions indexOptions, Class<?> clazz);

    List<String> createIndexes(List<IndexModel> indexes, Class<?> clazz);

    List<String> createIndexes(List<IndexModel> indexes, CreateIndexOptions createIndexOptions, Class<?> clazz);

    List<Document> listIndexes(Class<?> clazz);

    void dropIndex(String indexName,Class<?> clazz);

    void dropIndex(String indexName, DropIndexOptions dropIndexOptions, Class<?> clazz);

    void dropIndex(Bson keys,Class<?> clazz);

    void dropIndex(Bson keys,DropIndexOptions dropIndexOptions,Class<?> clazz);

    void dropIndexes(Class<?> clazz);

    void dropIndexes(DropIndexOptions dropIndexOptions,Class<?> clazz);

}
