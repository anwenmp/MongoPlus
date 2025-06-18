package com.mongoplus.handlers.field;

import com.mongodb.BasicDBObject;
import com.mongoplus.annotation.collection.CollectionField;
import com.mongoplus.conditions.interfaces.condition.CompareCondition;
import com.mongoplus.handlers.condition.ConditionHandler;
import com.mongoplus.toolkit.ObjectIdUtil;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ObjectId类型查询参数处理
 * @author anwen
 */
public class ObjectIdHandler implements ConditionHandler {

    private final Map<Field,Boolean> fieldCache = new ConcurrentHashMap<>();

    @Override
    public void beforeQueryCondition(CompareCondition compareCondition, BasicDBObject basicDBObject) {
        handler(compareCondition);
    }

    @Override
    public void beforeUpdateCondition(CompareCondition compareCondition, BasicDBObject basicDBObject) {
        handler(compareCondition);
    }

    public void handler(CompareCondition compareCondition) {
        Field originalField = compareCondition.getOriginalField();
        if (originalField != null) {
            Boolean isObjectId = fieldCache.computeIfAbsent(originalField, k -> {
                boolean _b = originalField.isAnnotationPresent(CollectionField.class);
                if (_b) {
                    CollectionField collectionField = originalField.getAnnotation(CollectionField.class);
                    _b = collectionField.isObjectId();
                }
                return _b;
            });
            if (isObjectId) {
                compareCondition.setValue(ObjectIdUtil.getObjectIdValue(compareCondition.getValue()));
            }
        }
    }

}
