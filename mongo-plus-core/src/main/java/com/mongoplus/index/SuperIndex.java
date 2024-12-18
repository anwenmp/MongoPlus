package com.mongoplus.index;

import com.mongodb.client.model.CreateIndexOptions;
import com.mongodb.client.model.DropIndexOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

public interface SuperIndex {

    String createIndex(String database, String collectionName, Bson bson);

    String createIndex(String database,String collectionName,Bson bson, IndexOptions indexOptions);

    List<String> createIndexes(String database, String collectionName, List<IndexModel> indexes);

    List<String> createIndexes(String database,String collectionName,List<IndexModel> indexes, CreateIndexOptions createIndexOptions);

    List<Document> listIndexes(String database, String collectionName);

    void dropIndex(String database,String collectionName,String indexName);

    void dropIndex(String database,String collectionName,String indexName, DropIndexOptions dropIndexOptions);

    void dropIndex(String database,String collectionName,Bson keys);

    void dropIndex(String database,String collectionName,Bson keys,DropIndexOptions dropIndexOptions);

    void dropIndexes(String database,String collectionName);

    void dropIndexes(String database,String collectionName,DropIndexOptions dropIndexOptions);

}
