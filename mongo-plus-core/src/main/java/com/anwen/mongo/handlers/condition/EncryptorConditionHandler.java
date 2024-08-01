package com.anwen.mongo.handlers.condition;

import com.anwen.mongo.annotation.comm.FieldEncrypt;
import com.anwen.mongo.conditions.interfaces.condition.CompareCondition;
import com.anwen.mongo.toolkit.EncryptorUtil;
import com.mongodb.BasicDBObject;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 加密条件处理器
 *
 * @author anwen
 */
public class EncryptorConditionHandler implements ConditionHandler {

    public final Map<Field,FieldEncrypt> fieldFieldEncryptMap = new ConcurrentHashMap<>();

    public final Map<Field,Boolean> fieldEncryptPresent = new ConcurrentHashMap<>();

    @Override
    public void beforeQueryCondition(CompareCondition compareCondition, BasicDBObject basicDBObject) {
        Field originalField = compareCondition.getOriginalField();
        if (originalField != null){
            Boolean existFieldEncrypt = fieldEncryptPresent.computeIfAbsent(originalField,field -> field.isAnnotationPresent(FieldEncrypt.class));
            if (!existFieldEncrypt){
                return;
            }
            FieldEncrypt fieldEncrypt = fieldFieldEncryptMap.computeIfAbsent(originalField,field -> field.getAnnotation(FieldEncrypt.class));
            compareCondition.setValue(EncryptorUtil.encrypt(fieldEncrypt,compareCondition.getValue()));
        }
    }
}
