package com.mongoplus.conditions.interfaces.query;

import com.mongoplus.conditions.interfaces.BaseCondition;
import com.mongoplus.model.Projection;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.constant.SqlOperationConstant;
import com.mongoplus.model.Order;
import com.mongoplus.support.SFunction;
import com.mongoplus.toolkit.ObjectIdUtil;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 基础查询接口
 *
 * @author anwen
 */
public interface BaseQueryCondition<T, Children> extends BaseCondition<T, Children> {

    /**
     * 添加条件
     *
     * @param conditionMetaObject 条件对象
     * @return this
     */
    Children addCondition(ConditionMetaObject conditionMetaObject);

    /**
     * 添加条件
     * @param projections project条件
     * @return this
     */
    Children addCondition(List<Projection> projections);

    /**
     * 添加条件
     * @param order order条件
     * @return this
     */
    Children addCondition(Order order);

    default ConditionMetaObject getBaseCondition(String column, Object value){
        return getBaseCondition(
                Thread.currentThread().getStackTrace()[2].getMethodName(),
                column,
                value,
                Object.class,
                null,
                null
        );
    }

    default ConditionMetaObject getBaseConditionExtraValue(String column, Object value, Object extraValue){
        return getBaseCondition(
                Thread.currentThread().getStackTrace()[2].getMethodName(),
                column,
                value,
                Object.class,
                null,
                extraValue
        );
    }

    default ConditionMetaObject getBaseCondition(
            String condition,
            String column,
            Object value,
            Class<?> clazz,
            Field field,
            Object extraValue) {
        if (Objects.equals(column, SqlOperationConstant._ID)) {
            if (value instanceof Collection<?>) {
                value = ((Collection<?>) value).stream()
                        .map(ObjectIdUtil::getObjectIdValue)
                        .collect(Collectors.toList());
            } else {
                value = ObjectIdUtil.getObjectIdValue(value);
            }
        }
        return new ConditionMetaObject(condition, column, value,clazz,field,extraValue);
    }

    default ConditionMetaObject getBaseCondition(SFunction<T, ?> column, Object value){
        return getBaseCondition(
                Thread.currentThread().getStackTrace()[2].getMethodName(),
                column.getFieldNameLine(),
                value,
                column.getImplClass(),
                column.getField(),
                null
        );
    }

    default ConditionMetaObject getBaseConditionExtraValue(SFunction<T, ?> column, Object value, Object extraValue) {
        return getBaseCondition(
                Thread.currentThread().getStackTrace()[2].getMethodName(),
                column.getFieldNameLine(),
                value,
                column.getImplClass(),
                column.getField(),
                extraValue
        );
    }

    default ConditionMetaObject getBaseCondition(String methodName,SFunction<T, ?> column, Object value){
        return getBaseCondition(
                methodName,
                column.getFieldNameLine(),
                value,
                column.getImplClass(),
                column.getField(),
                null
        );
    }

    default ConditionMetaObject getBaseCondition(QueryChainWrapper<?,?> queryChainWrapper){
        return ConditionMetaObject
                .builder()
                .condition(Thread.currentThread().getStackTrace()[2].getMethodName())
                .value(queryChainWrapper)
                .build();
    }

    default ConditionMetaObject getBaseCondition(Object value){
        return new ConditionMetaObject(
                Thread.currentThread().getStackTrace()[2].getMethodName(),
                value,
                Object.class,
                null
        );
    }

    default ConditionMetaObject getBaseConditionExtraValue(Object value,Object extraValue){
        return new ConditionMetaObject(
                Thread.currentThread().getStackTrace()[2].getMethodName(),
                value,
                Object.class,
                null,
                extraValue
        );
    }

}
