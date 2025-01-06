package com.mongoplus.handlers.condition;

import com.mongodb.BasicDBObject;
import com.mongoplus.annotation.comm.FieldEncrypt;
import com.mongoplus.conditions.interfaces.condition.CompareCondition;
import com.mongoplus.toolkit.EncryptorUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
            Object value = compareCondition.getValue();
            if (value instanceof Collection) {
                List<Object> encryptValueList = new ArrayList<>();
                ((Collection<?>) value).forEach(o -> encryptValueList.add(EncryptorUtil.encrypt(fieldEncrypt,o)));
                value = encryptValueList;
            } else {
                value = EncryptorUtil.encrypt(fieldEncrypt,value);
            }
            compareCondition.setValue(value);
        }
    }
}
