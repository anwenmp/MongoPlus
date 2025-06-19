package com.mongoplus.interceptor;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.WriteModel;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.manager.SensitiveWordManager;
import com.mongoplus.model.MutablePair;
import com.mongoplus.registry.HandlerRegistry;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

/**
 * 关键字拦截器
 */
public class SensitiveWordInterceptor implements Interceptor {

    private final SensitiveWordManager sensitiveWordManager;

    public SensitiveWordInterceptor() {
        sensitiveWordManager = HandlerRegistry.get(SensitiveWordManager.class);
        if (sensitiveWordManager == null) {
            throw new MongoPlusException("SensitiveWordManager is null");
        }
    }

    @Override
    public List<Document> executeSave(List<Document> documentList, MongoCollection<Document> collection) {
        documentList.forEach(document -> {
            String documentJson = document.toJson(MapCodecCache.getDefaultCodec());
            sensitiveWordManager.handler(documentJson);
        });
        return documentList;
    }

    @Override
    public Document executeSave(Document document, MongoCollection<Document> collection) {
        String documentJson = document.toJson(MapCodecCache.getDefaultCodec());
        sensitiveWordManager.handler(documentJson);
        return document;
    }

    @Override
    public List<MutablePair<Bson, Bson>> executeUpdate(List<MutablePair<Bson, Bson>> updatePairList,
                                                       MongoCollection<Document> collection) {
        updatePairList.forEach(pair -> {
            String documentJson = pair.getRight()
                    .toBsonDocument(BsonDocument.class, MapCodecCache.getDefaultCodecRegistry())
                    .toString();
            sensitiveWordManager.handler(documentJson);
        });
        return updatePairList;
    }

    @Override
    public MutablePair<Bson, Bson> executeUpdate(MutablePair<Bson, Bson> updatePair, MongoCollection<Document> collection) {
        String documentJson = updatePair.getRight()
                .toBsonDocument(BsonDocument.class, MapCodecCache.getDefaultCodecRegistry())
                .toString();
        sensitiveWordManager.handler(documentJson);
        return updatePair;
    }

    @Override
    public List<WriteModel<Document>> executeBulkWrite(List<WriteModel<Document>> writeModelList,
                                                       MongoCollection<Document> collection) {
        writeModelList.forEach(writeModel -> {
            String documentJson;
            if (writeModel instanceof InsertOneModel) {
                documentJson = writeModel.toString();
            } else {
                UpdateManyModel<Document> updateManyModel = (UpdateManyModel<Document>) writeModel;
                documentJson = (updateManyModel.getUpdate() != null ?
                        updateManyModel.getUpdate()
                                .toBsonDocument(
                                        BsonDocument.class,
                                        MapCodecCache.getDefaultCodecRegistry()
                                ).toString() : String.valueOf(updateManyModel.getUpdatePipeline()));
            }
            sensitiveWordManager.handler(documentJson);
        });
        return writeModelList;
    }
}
