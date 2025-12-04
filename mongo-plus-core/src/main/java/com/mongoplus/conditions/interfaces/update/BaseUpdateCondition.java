package com.mongoplus.conditions.interfaces.update;

import com.mongoplus.conditions.interfaces.BaseCondition;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.support.SFunction;

/**
 * 基础修改接口
 * @author anwen
 */
public interface BaseUpdateCondition<T, Children> extends BaseCondition<T, Children> {

    /**
     * 添加条件
     * @param conditionMetaObject 条件对象
     * @return {@link Children}
     * @author anwen
     */
    Children addUpdateCondition(ConditionMetaObject conditionMetaObject);

    default ConditionMetaObject getBaseUpdateCompare(SFunction<T, Object> column, Object value, Object extraValue) {
        return new ConditionMetaObject(
                Thread.currentThread().getStackTrace()[2].getMethodName(),
                column.getFieldNameLine(),
                value,
                column.getImplClass(),
                column.getField(),
                extraValue
        );
    }

    default ConditionMetaObject getBaseUpdateCompare(SFunction<T, Object> column, Object value) {
        return new ConditionMetaObject(
                Thread.currentThread().getStackTrace()[2].getMethodName(),
                column.getFieldNameLine(),
                value,
                column.getImplClass(),
                column.getField()
        );
    }

    default ConditionMetaObject getBaseUpdateCompare(Object value) {
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        return new ConditionMetaObject(methodName,methodName,value,Object.class,null);
    }

    default ConditionMetaObject getBaseUpdateCompare(Object value,Object extraValue) {
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        return new ConditionMetaObject(methodName,methodName,value,Object.class,null,extraValue);
    }

    default ConditionMetaObject getBaseUpdateCompare(String column, Object value) {
        return new ConditionMetaObject(
                Thread.currentThread().getStackTrace()[2].getMethodName(),
                column,
                value,
                Object.class,
                null
        );
    }

    default ConditionMetaObject getBaseUpdateCompare(String column, Object value,Object extraValue) {
        return new ConditionMetaObject(
                Thread.currentThread().getStackTrace()[2].getMethodName(),
                column,
                value,
                Object.class,
                null,
                extraValue
        );
    }

}
