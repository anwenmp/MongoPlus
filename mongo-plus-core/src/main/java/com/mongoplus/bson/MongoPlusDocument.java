package com.mongoplus.bson;

import com.mongoplus.support.SFunction;
import org.bson.Document;

import java.util.Map;

/**
 * 可以使用lambda的Document
 *
 * @author JiaChaoYang
 **/
public class MongoPlusDocument extends Document {

    public MongoPlusDocument() {
    }

    public <T,R> MongoPlusDocument(SFunction<T,R> key,Object value){
        super(key.getFieldNameLine(),value);
    }

    public MongoPlusDocument(String key, Object value) {
        super(key, value);
    }

    public MongoPlusDocument(Map<String, ?> map) {
        super(map);
    }

    public <T,R> void put(SFunction<T,R> key, Object value){
        super.put(key.getFieldNameLine(),value);
    }

    public <T,R> void putIsNotNull(String key, Object value){
        if (value != null) {
            super.put(key, value);
        }
    }

    public <T,V> void put(SFunction<T,?> key, SFunction<V,?> value){
        super.put(key.getFieldNameLine(),value.getFieldNameLine());
    }

    public <T,V> void putOption(SFunction<T,?> key, SFunction<V,?> value){
        super.put(key.getFieldNameLine(),value.getFieldNameLineOption());
    }

    public <T,R> void append(SFunction<T,R> key,Object value){
        super.append(key.getFieldNameLine(),value);
    }

    public <T,R> Object get(SFunction<T,R> key){
        return super.get(key.getFieldNameLine());
    }

    public <T,R,D> D get(SFunction<T,R> key,Class<D> clazz){
        return super.get(key.getFieldNameLine(),clazz);
    }

    public <T,R> Object remove(SFunction<T,R> key){
        return super.remove(key.getFieldNameLine());
    }

    public <T,R> Object remove(SFunction<T,R> key,Object value){
        return super.remove(key.getFieldNameLine(),value);
    }

    public <T,R> boolean containsKey(SFunction<T,R> key){
        return super.containsKey(key.getFieldNameLine());
    }
}
