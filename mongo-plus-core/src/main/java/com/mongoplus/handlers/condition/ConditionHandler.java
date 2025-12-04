package com.mongoplus.handlers.condition;

import com.mongodb.BasicDBObject;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;

/**
 * 条件处理器
 *
 * @author anwen
 */
public interface ConditionHandler {

    /**
     * 查询条件前置处理器
     * @param conditionMetaObject 条件
     * @param basicDBObject 最终对象
     * @author anwen
     */
    default void beforeQueryCondition(ConditionMetaObject conditionMetaObject, BasicDBObject basicDBObject){}

    /**
     * 查询条件后置处理器
     * @param conditionMetaObject 条件
     * @param basicDBObject 最终对象
     * @author anwen
     */
    default void afterQueryCondition(ConditionMetaObject conditionMetaObject, BasicDBObject basicDBObject){}

    /**
     * 修改条件前置拦截器
     * @param conditionMetaObject 条件
     * @param basicDBObject 最终对象
     * @author anwen
     */
    default void beforeUpdateCondition(ConditionMetaObject conditionMetaObject, BasicDBObject basicDBObject){}

    /**
     * 修改条件后置拦截器
     * @param conditionMetaObject 条件
     * @param basicDBObject 最终对象
     * @author anwen
     */
    default void afterUpdateCondition(ConditionMetaObject conditionMetaObject, BasicDBObject basicDBObject){}

}
