package com.mongoplus.handlers.condition;

import com.mongodb.BasicDBObject;
import com.mongoplus.conditions.interfaces.condition.CompareCondition;

/**
 * 条件处理器
 *
 * @author anwen
 * @date 2024/6/30 下午3:57
 */
public interface ConditionHandler {

    /**
     * 查询条件前置处理器
     * @param compareCondition 条件
     * @param basicDBObject 最终对象
     * @author anwen
     * @date 2024/6/30 下午4:01
     */
    default void beforeQueryCondition(CompareCondition compareCondition,BasicDBObject basicDBObject){}

    /**
     * 查询条件后置处理器
     * @param compareCondition 条件
     * @param basicDBObject 最终对象
     * @author anwen
     * @date 2024/8/1 下午2:36
     */
    default void afterQueryCondition(CompareCondition compareCondition,BasicDBObject basicDBObject){}

    /**
     * 修改条件前置拦截器
     * @param compareCondition 条件
     * @param basicDBObject 最终对象
     * @author anwen
     * @date 2024/6/30 下午4:01
     */
    default void beforeUpdateCondition(CompareCondition compareCondition,BasicDBObject basicDBObject){}

    /**
     * 修改条件后置拦截器
     * @param compareCondition 条件
     * @param basicDBObject 最终对象
     * @author anwen
     * @date 2024/8/1 下午2:36
     */
    default void afterUpdateCondition(CompareCondition compareCondition,BasicDBObject basicDBObject){}

}
