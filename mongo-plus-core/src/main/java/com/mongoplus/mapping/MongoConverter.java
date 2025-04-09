package com.mongoplus.mapping;

import com.mongodb.client.MongoIterable;
import com.mongoplus.annotation.ID;
import com.mongoplus.constant.SqlOperationConstant;
import com.mongoplus.domain.MongoPlusFieldException;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.ClassTypeUtil;
import org.bson.Document;

import java.util.*;

import static com.mongoplus.convert.Converter.convertKeysToCamelCase;

/**
 * 将对象映射为Document和将Document映射为对象
 * @author JiaChaoYang
*/
public interface MongoConverter extends MongoWriter,EntityRead {

    /**
     * 添加的映射器
     * @author JiaChaoYang
     */
    void writeBySave(Object sourceObj, Document document);

    /**
     * 添加的映射器
     * @author JiaChaoYang
     */
    default Document writeBySave(Object sourceObj){
        Document document = new Document();
        writeBySave(sourceObj,document);
        return document;
    }

    /**
     * map映射到document
     * @author anwen
     */
    default Document write(Map<String,Object> map){
        Document document = new Document();
        write(map,document);
        return document;
    }

    /**
     * 批量映射
     * @author anwen
     */
    default List<Document> writeBatch(Collection<Map<String,Object>> sourceObjCollection, List<Document> documentList){
        sourceObjCollection.forEach(sourceObj -> documentList.add(write(sourceObj)));
        return documentList;
    }

    /**
     * 批量映射
     * @author anwen
     */
    default List<Document> writeBatch(Collection<Map<String,Object>> sourceObjCollection){
        List<Document> documentList = new ArrayList<>();
        sourceObjCollection.forEach(sourceObj -> documentList.add(write(sourceObj)));
        return documentList;
    }

    /**
     * 批量映射
     * @author anwen
     */
    default void writeBySaveBatch(Collection<?> sourceObjCollection, List<Document> documentList){
        sourceObjCollection.forEach(sourceObj -> {
            Document document = new Document();
            writeBySave(sourceObj,document);
            documentList.add(document);
        });
    }

    /**
     * 批量映射
     * @author anwen
     */
    default List<Document> writeBySaveBatch(Collection<?> sourceObjCollection){
        List<Document> documentList = new ArrayList<>();
        sourceObjCollection.forEach(sourceObj -> {
            Document document = new Document();
            writeBySave(sourceObj,document);
            documentList.add(document);
        });
        return documentList;
    }

    void writeByUpdate(Object sourceObj, Document document);

    default Document writeByUpdate(Object sourceObj){
        Document document = new Document();
        writeByUpdate(sourceObj,document);
        return document;
    }

    default void writeByUpdateBatch(Collection<?> sourceObjCollection, List<Document> documentList){
        sourceObjCollection.forEach(sourceObj -> {
            Document document = new Document();
            writeByUpdate(sourceObj,document);
            documentList.add(document);
        });
    }

    default List<Document> writeByUpdateBatch(Collection<?> sourceObjCollection){
        List<Document> documentList = new ArrayList<>();
        sourceObjCollection.forEach(sourceObj -> {
            Document document = new Document();
            writeByUpdate(sourceObj,document);
            documentList.add(document);
        });
        return documentList;
    }

    /**
     * 写内部属性
     * @author anwen
     */
    default <T> T readInternal(Document document, Class<T> clazz){
        return readInternal(document,new TypeReference<T>(clazz){});
    }

    /**
     * 写内部属性
     * @author anwen
     */
    <T> T readInternal(Object sourceObj, TypeReference<T> typeReference);

    /**
     * 写为Class
     * @author anwen
     */
    default <T> List<T> read(MongoIterable<Document> findIterable, Class<T> clazz) {
        List<T> resultList = new ArrayList<>();
        findIterable.forEach(document -> resultList.add(convertDocument(document,clazz)));
        return resultList;
    }

    /**
     * 写为class，根据传入的type
     * @author anwen
     */
    default <T> List<T> read(MongoIterable<Document> findIterable, TypeReference<T> typeReference){
        List<T> resultList = new ArrayList<>();
        findIterable.forEach(document -> resultList.add(read(document, typeReference)));
        return resultList;
    }

    /**
     * 写为class
     * @author anwen
     */
    @SuppressWarnings("unchecked")
    default <T> T readDocument(MongoIterable<Document> findIterable,Class<?> clazz){
        Document document = findIterable.first();
        if (document != null){
            return (T) convertDocument(document, clazz);
        }
        return null;
    }

    /**
     * 写为class
     * @author anwen
     */
    default <T> T readDocument(MongoIterable<Document> findIterable,TypeReference<T> typeReference){
        Document document = findIterable.first();
        if (document != null){
            return read(document, typeReference);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    default  <T> T convertDocument(Document document, Class<T> clazz) {
        if (ClassTypeUtil.isTargetClass(Map.class,clazz)) {
            return (T) convertKeysToCamelCase(document);
        } else {
            return read(document, clazz);
        }
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    default void reSetIdValue(Object sourceObj, Document document) {
        if (Objects.isNull(sourceObj) || Objects.isNull(document) || !document.containsKey(SqlOperationConstant._ID)) {
            return;
        }
        // Map类型不需要再做下边的操作 因为它们只针对实体类
        if (ClassTypeUtil.isTargetClass(Map.class,sourceObj.getClass())){
            Map map = (Map) sourceObj;
            if (!map.containsKey(SqlOperationConstant._ID)){
                map.put(SqlOperationConstant._ID, document.get(SqlOperationConstant._ID));
            }
            return;
        }
        TypeInformation typeInformation = TypeInformation.of(sourceObj);
        FieldInformation idFieldInformation = typeInformation.getAnnotationField(ID.class, "@ID field not found");
        Object idValue = idFieldInformation.getValue();
        if (Objects.isNull(idValue)) {
            try {
                Object idV = document.get(SqlOperationConstant._ID);
                ConversionStrategy<?> conversionStrategy = getConversionStrategy(idFieldInformation.getTypeClass());
                if (conversionStrategy != null && idV.getClass() != idFieldInformation.getTypeClass()){
                    idFieldInformation.setValue(conversionStrategy.convertValue(idV,idFieldInformation.getTypeClass(),this));
                } else {
                    idFieldInformation.setValue(idV);
                }
            } catch (Exception e) {
                throw new MongoPlusFieldException("reSet id value error", e);
            }
        }
    }

    default <T> void batchReSetIdValue(Collection<T> entityList, List<Document> documentList) {
        int index = 0;
        for (T t : entityList) {
            Document document = documentList.get(index);
            reSetIdValue(t, document);
            index++;
        }
    }

    ConversionStrategy<?> getConversionStrategy(Class<?> target);
}
