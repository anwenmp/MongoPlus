package com.mongoplus.bson;

import com.mongodb.BasicDBObject;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.support.SFunction;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

/**
 * 支持lambda的BasicDBObject
 * @author JiaChaoYang
 **/
public class MongoPlusBasicDBObject extends BasicDBObject {

    public <T,R> void put(SFunction<T,R> key,BasicDBObject value){
        put(key.getFieldNameLine(),value);
    }

    public <T,R> void append(SFunction<T,R> key,BasicDBObject value){
        super.append(key.getFieldNameLine(),value);
    }

    public <T,R> void get(SFunction<T,R> key){
        super.get(key.getFieldNameLine());
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

    public <T,R> boolean containsKey(SFunction<T,R> key){
        return super.containsKey(key.getFieldNameLine());
    }

    public <T,R> Object remove(SFunction<T,R> key){
        return super.remove(key.getFieldNameLine());
    }

    public void put(String key,BasicDBObject value){
        if (containsKey(key)){
            super.put(key,new BasicDBObject((BasicDBObject) get(key)){{
                value.keySet().forEach(basic -> {
                    append(basic,value.get(basic));
                });
            }});
        }else {
            super.put(key,value);
        }
    }

    public void put(Bson bson){
        BsonDocument superBsonDocument = super.toBsonDocument(BsonDocument.class, MapCodecCache.getDefaultCodecRegistry());
        BsonDocument bsonDocument = bson.toBsonDocument(BsonDocument.class, MapCodecCache.getDefaultCodecRegistry());
        bsonDocument.forEach((k,v) -> {
            if (super.containsKey(k)){
                Object value = get(k);
                if (value instanceof BsonDocument){
                    ((BsonDocument) value).putAll(v.asDocument());
                }
            }else {
                super.putAll(bsonDocument);
            }
        });
    }
}
