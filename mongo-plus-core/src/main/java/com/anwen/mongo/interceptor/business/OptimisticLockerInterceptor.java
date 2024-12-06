package com.anwen.mongo.interceptor.business;

import com.anwen.mongo.annotation.collection.Version;
import com.anwen.mongo.enums.UpdateConditionEnum;
import com.anwen.mongo.interceptor.Interceptor;
import com.anwen.mongo.logging.Log;
import com.anwen.mongo.logging.LogFactory;
import com.anwen.mongo.mapping.FieldInformation;
import com.anwen.mongo.mapping.TypeInformation;
import com.anwen.mongo.model.MutablePair;
import com.anwen.mongo.registry.MongoEntityMappingRegistry;
import com.anwen.mongo.toolkit.BsonUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.anwen.mongo.enums.UpdateConditionEnum.INC;

/**
 * 乐观锁
 * @author anwen
 */
public class OptimisticLockerInterceptor implements Interceptor {

    private Log log = LogFactory.getLog(OptimisticLockerInterceptor.class);

    private final Map<Class<?>,FieldInformation> optimisticLockerExistMap = new ConcurrentHashMap<>();

    /**
     * 自增数，默认为1
     */
    private Integer autoInc = 1;

    /**
     * 设置乐观锁字段自增数
     * @author anwen
     */
    public void setAutoInc(Integer autoInc){
        this.autoInc = autoInc;
    }

    @Override
    public List<Document> executeSave(List<Document> documentList, MongoCollection<Document> collection) {
        FieldInformation fieldInformation = getVersionFieldInformation(collection);
        if (fieldInformation == null){
            return documentList;
        }
        String fieldName = fieldInformation.getCamelCaseName();
        documentList.stream()
                .filter(document -> !document.containsKey(fieldName) || document.get(fieldName) == null)
                .forEach(document -> document.put(fieldName,0));
        return documentList;
    }

    @Override
    public List<MutablePair<Bson,Bson>> executeUpdate(List<MutablePair<Bson,Bson>> updatePairList,
                                                      MongoCollection<Document> collection){
        FieldInformation fieldInformation = getVersionFieldInformation(collection);
        if (fieldInformation == null){
            return updatePairList;
        }
        String fieldName = fieldInformation.getCamelCaseName();
        Document valueDocument = new Document(INC.getCondition(), new Document(fieldName, autoInc));
        updatePairList.forEach(updatePair -> {
            handlerUpdate(fieldName,updatePair.getLeft(),updatePair.getRight());
        });
        return updatePairList;
    }

    @Override
    public List<WriteModel<Document>> executeBulkWrite(List<WriteModel<Document>> writeModelList,
                                                       MongoCollection<Document> collection) {
        FieldInformation fieldInformation = getVersionFieldInformation(collection);
        if (fieldInformation == null){
            return writeModelList;
        }
        String fieldName = fieldInformation.getCamelCaseName();
        writeModelList.forEach(writeModel -> {
            if (writeModel instanceof InsertOneModel) {
                InsertOneModel<Document> insertOneModel = (InsertOneModel<Document>) writeModel;
                handlerSave(fieldName,insertOneModel.getDocument());
            }
            if (writeModel instanceof UpdateManyModel){
                UpdateManyModel<Document> updateManyModel = (UpdateManyModel<Document>) writeModel;
                Bson filterBson = updateManyModel.getFilter();
                Bson updateBson = updateManyModel.getUpdate();
                handlerUpdate(fieldName,filterBson,updateBson);
            }
        });
        return writeModelList;
    }

    @Override
    public int order() {
        return 2;
    }

    void handlerSave(String fieldName, Document document){
        if (!document.containsKey(fieldName) || document.get(fieldName) == null) {
            document.put(fieldName, 0);
        }
    }

    void handlerUpdate(String fieldName,Bson filterBson,Bson updateBson){
        Document valueDocument = new Document(INC.getCondition(), new Document(fieldName, autoInc));
        Document document = BsonUtil.asDocument(updateBson);
        Document setDocument = document.get(UpdateConditionEnum.SET.getCondition(), Document.class);
        Object versionValue = setDocument.get(fieldName);
        if (versionValue == null){
            log.debug("There is an optimistic lock field, but the original value of the optimistic lock has not been obtained,fieldName: "+fieldName);
            return;
        }
        BsonUtil.addToMap(filterBson,fieldName,versionValue);
        BsonUtil.removeFrom(setDocument,fieldName);
        BsonUtil.addAllToMap(updateBson, valueDocument);
    }

    FieldInformation getVersionFieldInformation(MongoCollection<Document> collection){
        String fullName = collection.getNamespace().getFullName();
        Class<?> clazz;
        if (null == (clazz = MongoEntityMappingRegistry.getInstance()
                .getMappingResource(fullName))){
            return null;
        }
        return optimisticLockerExistMap.computeIfAbsent(clazz, k -> {
            TypeInformation typeInformation = TypeInformation.of(clazz);
            return typeInformation.getAnnotationField(Version.class);
        });
    }

}
