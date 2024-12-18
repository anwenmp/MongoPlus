package com.mongoplus.index;

import com.mongodb.client.model.CreateIndexOptions;
import com.mongodb.client.model.DropIndexOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

import static com.mongoplus.toolkit.StringPool.EMPTY;

public interface Index extends SuperIndex {

    default String createIndex(String collectionName, Bson bson){
        return createIndex(EMPTY,collectionName,bson);
    }

    default String createIndex(String collectionName,Bson bson, IndexOptions indexOptions){
        return createIndex(EMPTY,collectionName,bson,indexOptions);
    }

    default List<String> createIndexes(String collectionName, List<IndexModel> indexes){
        return createIndexes(EMPTY,collectionName,indexes);
    }

    default List<String> createIndexes(String collectionName,List<IndexModel> indexes, CreateIndexOptions createIndexOptions){
        return createIndexes(EMPTY,collectionName,indexes,createIndexOptions);
    }

    default List<Document> listIndexes(String collectionName){
        return listIndexes(EMPTY,collectionName);
    }

    default void dropIndex(String collectionName,String indexName){
        dropIndex(EMPTY,collectionName,indexName);
    }

    default void dropIndex(String collectionName,String indexName, DropIndexOptions dropIndexOptions){
        dropIndex(EMPTY,collectionName,indexName,dropIndexOptions);
    }

    default void dropIndex(String collectionName,Bson keys){
        dropIndex(EMPTY,collectionName,keys);
    }

    default void dropIndex(String collectionName,Bson keys,DropIndexOptions dropIndexOptions){
        dropIndex(EMPTY,collectionName,keys,dropIndexOptions);
    }

    default void dropIndexes(String collectionName){
        dropIndex(EMPTY,collectionName);
    }

    default void dropIndexes(String collectionName,DropIndexOptions dropIndexOptions){
        dropIndexes(EMPTY,collectionName,dropIndexOptions);
    }

}
