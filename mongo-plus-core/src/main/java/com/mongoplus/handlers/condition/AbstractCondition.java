package com.mongoplus.handlers.condition;

import com.mongodb.BasicDBObject;
import com.mongoplus.annotation.comm.EnumValue;
import com.mongoplus.bson.MongoPlusBasicDBObject;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.cache.global.HandlerCache;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.conditions.update.UpdateChainWrapper;
import com.mongoplus.enums.UpdateConditionEnum;
import com.mongoplus.model.BuildUpdate;
import com.mongoplus.model.MutablePair;
import com.mongoplus.toolkit.ClassTypeUtil;
import com.mongoplus.toolkit.CollUtil;
import org.bson.BsonDocument;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 抽象的条件处理器
 *
 * @author anwen
 */
public abstract class AbstractCondition implements Condition, UpdateCondition {

    /**
     * 用于缓存每个枚举类型的字段与EnumValue注解的映射
     */
    final Map<Class<?>, Field> enumValueCache = new ConcurrentHashMap<>();

    @Override
    public BasicDBObject queryCondition(List<ConditionMetaObject> conditionMetaObjectList) {
        MongoPlusBasicDBObject mongoPlusBasicDBObject = new MongoPlusBasicDBObject();
        if (CollUtil.isNotEmpty(conditionMetaObjectList)) {
            conditionMetaObjectList.forEach(compareCondition -> {
                checkCompareCondition(compareCondition);
                queryCondition(compareCondition, mongoPlusBasicDBObject);
            });
        }
        return mongoPlusBasicDBObject;
    }

    @Override
    public BasicDBObject queryCondition(ConditionMetaObject conditionMetaObject) {
        return queryCondition(conditionMetaObject, new MongoPlusBasicDBObject());
    }

    @Override
    public MutablePair<BasicDBObject, BasicDBObject> updateCondition(UpdateChainWrapper<?, ?> updateChainWrapper) {
        List<ConditionMetaObject> updateCompareList = updateChainWrapper.getUpdateCompareList();

        Map<UpdateConditionEnum, List<ConditionMetaObject>> conditionMap = Arrays.stream(UpdateConditionEnum.values())
                .collect(Collectors.toMap(Function.identity(),
                        conditionEnum -> updateCompareList.stream()
                                .filter(compareCondition -> Objects.equals(compareCondition.getCondition(),
                                        conditionEnum.getSubCondition()))
                                .collect(Collectors.toList())));

        BasicDBObject updateBasicDBObject = new BasicDBObject();
        conditionMap.forEach((conditionEnum, compareConditionList) -> {
            if (CollUtil.isNotEmpty(compareConditionList)) {
                updateBasicDBObject.append(conditionEnum.getCondition(), updateValue(conditionEnum, compareConditionList));
            }
        });
        if (CollUtil.isNotEmpty(updateChainWrapper.getUpdateBson())) {
            updateChainWrapper.getUpdateBson().forEach(updateBson ->
                    updateBasicDBObject.putAll(updateBson.toBsonDocument(
                            BsonDocument.class,
                            MapCodecCache.getDefaultCodecRegistry())));
        }
        return new MutablePair<>(updateChainWrapper.buildCondition().getCondition(), updateBasicDBObject);
    }

    /**
     * 具体的抽象更新构建方法
     *
     * @param updateConditionEnum  操作枚举
     * @param conditionMetaObjectList 条件集合
     * @return {@link BasicDBObject}
     * @author anwen
     */
    public BasicDBObject updateValue(UpdateConditionEnum updateConditionEnum, List<ConditionMetaObject> conditionMetaObjectList) {
        final AtomicReference<List<ConditionMetaObject>> finalCompareConditionList = new AtomicReference<>(conditionMetaObjectList);
        BiFunction<AbstractCondition, BuildUpdate, BasicDBObject> updateValueFunc = (condition, buildUpdate) -> {
            List<ConditionMetaObject> currentConditionMetaObjectList = finalCompareConditionList.get();
            switch (updateConditionEnum) {
                case SET:
                case INC:
                case MIN:
                case MAX:
                case MUL:
                case POP:
                case PULL_ALL:
                    return condition.buildUpdateCondition(currentConditionMetaObjectList, buildUpdate);
                case PUSH:
                    finalCompareConditionList.set(currentConditionMetaObjectList.stream().distinct().collect(Collectors.toList()));
                    currentConditionMetaObjectList = finalCompareConditionList.get();
                    return condition.buildPushCondition(currentConditionMetaObjectList, buildUpdate);
                case CURRENT_DATE:
                    return condition.buildCurrentDateCondition(currentConditionMetaObjectList, buildUpdate);
                case RENAME:
                    return condition.buildRenameCondition(currentConditionMetaObjectList, buildUpdate);
                case UNSET:
                    return condition.buildUnsetCondition(currentConditionMetaObjectList, buildUpdate);
                case ADD_TO_SET:
                    return condition.buildAddToSetCondition(currentConditionMetaObjectList, buildUpdate);
                case PULL:
                    return condition.buildPullCondition(currentConditionMetaObjectList, buildUpdate);
            }
            return null;
        };
        BasicDBObject updateBasicDBObject = new BasicDBObject();
        finalCompareConditionList.get().forEach(compareCondition -> {
            HandlerCache.conditionHandlerList.forEach(conditionHandler ->
                    conditionHandler.beforeUpdateCondition(compareCondition, updateBasicDBObject));
            updateValueFunc.apply(this, new BuildUpdate(compareCondition, updateBasicDBObject));
            HandlerCache.conditionHandlerList.forEach(conditionHandler ->
                    conditionHandler.afterUpdateCondition(compareCondition, updateBasicDBObject));
        });
        return updateBasicDBObject;
    }

    protected void checkCompareCondition(ConditionMetaObject conditionMetaObject) {
        Object value = conditionMetaObject.getValue();
        if (value == null) return;
        Object targetValue = value;
        Class<?> clazz = value.getClass();
        if (ClassTypeUtil.isTargetClass(Collection.class, clazz)) {
            targetValue = handleCollectionValue((Collection<?>) value);
        } else if (clazz.isEnum()) {
            targetValue = handleValue(clazz, value);
        }
        conditionMetaObject.setValue(targetValue);
    }

    protected Object handleCollectionValue(Collection<?> collection) {
        if (CollUtil.isEmpty(collection)) {
            return collection;
        }

        // 获取集合中第一个非空值并检查是否为枚举类型
        Object collectionValue = collection.stream()
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        if (collectionValue != null && collectionValue.getClass().isEnum()) {
            return collection.stream()
                    .map(o -> handleValue(collectionValue.getClass(), o))
                    .collect(Collectors.toList());
        }
        return collection;
    }

    protected Object handleValue(Class<?> clazz, Object value) {
        if (value == null) return null;
        // 从缓存中获取枚举值与字段的映射
        Field field = enumValueCache.computeIfAbsent(clazz, this::initFieldCache);
        if (field != null) {
            // 如果找到了字段，直接从该字段中获取值
            try {
                return field.get(value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            // 如果没有找到，使用枚举的名字
            return ((Enum<?>) value).name();
        }
    }

    /**
     * 创建枚举类型的字段与EnumValue注解的映射
     *
     * @param clazz 枚举类
     * @author anwen
     */
    protected Field initFieldCache(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            EnumValue enumValue = field.getAnnotation(EnumValue.class);
            if (enumValue != null) {
                // 如果找到了，但是不以该字段存储，则返回null
                if (!enumValue.valueStore()) {
                    return null;
                }
                return field;
            }
        }
        return null;
    }

    /**
     * 具体的抽象条件构建方法
     *
     * @param conditionMetaObject       条件
     * @param mongoPlusBasicDBObject BasicDBObject
     * @return {@link BasicDBObject}
     * @author anwen
     */
    public abstract BasicDBObject queryCondition(ConditionMetaObject conditionMetaObject, MongoPlusBasicDBObject mongoPlusBasicDBObject);

}
