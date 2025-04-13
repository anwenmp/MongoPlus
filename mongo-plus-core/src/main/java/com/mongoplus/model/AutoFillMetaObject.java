package com.mongoplus.model;

import com.mongoplus.bson.MongoPlusDocument;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.support.SFunction;
import org.bson.Document;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 自动填充元对象
 * @author JiaChaoYang
 */
public class AutoFillMetaObject {

    private final Log log = LogFactory.getLog(AutoFillMetaObject.class);

    /**
     * 需要自动填充的字段
     */
    private final MongoPlusDocument document;

    /**
     * 自动填充最终的值
     */
    private final ConcurrentSkipListMap<String,Object> autoFillDocument;

    /**
     * 跳过当前回写
     */
    private final AtomicBoolean isSkipWriteBack = new AtomicBoolean(false);

    /**
     * 原始对象信息
     */
    private TypeInformation targetObject;

    public AutoFillMetaObject() {
        this.document = new MongoPlusDocument();
        this.autoFillDocument = new ConcurrentSkipListMap<>();
    }

    public AutoFillMetaObject(MongoPlusDocument document) {
        this.document = document;
        this.autoFillDocument = new ConcurrentSkipListMap<>();
    }

    public AutoFillMetaObject(MongoPlusDocument document,TypeInformation typeInformation) {
        this.document = document;
        this.autoFillDocument = new ConcurrentSkipListMap<>();
        this.targetObject = typeInformation;
    }

    public AutoFillMetaObject(MongoPlusDocument document,ConcurrentSkipListMap<String,Object> autoFillDocument) {
        this.document = document;
        this.autoFillDocument = autoFillDocument;
    }

    /**
     * 获取所有的自动填充字段
     * @return {@link MongoPlusDocument}
     * @author anwen
     */
    public ConcurrentSkipListMap<String,Object> getAllFillField() {
        return autoFillDocument;
    }

    /**
     * 获取需要自动填充的字段
     * @author anwen
     */
    public MongoPlusDocument getDocument() {
        return document;
    }

    /**
     * 获取所有自动填充过的字段，并清空
     * @author anwen
     */
    public void getAllFillFieldAndClear(Document document){
        document.putAll(autoFillDocument);
        this.autoFillDocument.clear();
        this.targetObject = null;
    }

    /**
     * 是否存在自动填充的字段
     * @return {@link boolean}
     * @author anwen
     */
    public boolean isEmpty() {
        return document.isEmpty();
    }

    /**
     * 本次跳过字段回写
     * @author anwen
     */
    public void skipCurrentWriteBack() {
        this.isSkipWriteBack.set(true);
    }

    /**
     * 设置自动填充内容，如果字段不存在，则不填充
     * @param column 列名
     * @param value 值
     * @author anwen
     */
    public <T,R> void fillValue(SFunction<T,R> column,R value){
        fillValue(column.getFieldNameLine(), value);
    }

    /**
     * 设置自动填充内容，强制填充
     * @param column 列明
     * @param value 值
     * @author anwen
     */
    public <T,R> void forceFillValue(SFunction<T,R> column,R value){
        forceFillValue(column.getFieldNameLine(), value);
    }

    /**
     * 设置自动填充内容
     * @param column 列名
     * @param value 值
     * @author anwen
     */
    public void fillValue(String column,Object value) {
        if (metaObjectExist(column)) {
            forceFillValue(column,value);
        }
    }

    /**
     * 设置自动填充内容，强制填充
     * @param column 列明
     * @param value 值
     * @author anwen
     */
    public void forceFillValue(String column,Object value){
        autoFillDocument.put(column, value);
        if (isSkipWriteBack.compareAndSet(true, false)) {
            return;
        }
        FieldInformation fieldInformation = targetObject.getField(column);
        if (fieldInformation == null) {
            log.error("Autofill field not obtained, field name: "+ column);
            return;
        }
        fieldInformation.setValue(value);
    }

    /**
     * 指定的自动填充字段是否存在
     * @param column 列名
     * @return {@link boolean}
     * @author anwen
     */
    public <T,R> boolean metaObjectExist(SFunction<T,R> column){
        return document.containsKey(column);
    }

    /**
     * 指定的自动填充字段是否存在
     * @param column 列名
     * @return {@link boolean}
     * @author anwen
     */
    public boolean metaObjectExist(String column){
        return document.containsKey(column);
    }

    /**
     * 获取自动填充字段现有的值
     * @param column 列名
     * @return {@link Object}
     * @author anwen
     */
    public <T,R> Object getMetaObjectValue(SFunction<T,R> column){
        return document.get(column.getFieldNameLine());
    }

    /**
     * 获取自动填充字段现有的值
     * @param column 列名
     * @return {@link Object}
     * @author anwen
     */
    public Object getMetaObjectValue(String column){
        return document.get(column);
    }

    /**
     * 获取原始对象信息
     * @author anwen
     */
    public TypeInformation getTargetObject() {
        return targetObject;
    }

    /**
     * 设置原始对象信息
     * @param targetObject 对象
     * @author anwen
     */
    public void setTargetObject(TypeInformation targetObject) {
        this.targetObject = targetObject;
    }
}
