package com.mongoplus.conditions.interfaces.logic.operations;

import com.mongoplus.conditions.interfaces.condition.ConditionMetaObject;
import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.support.SFunction;

/**
 * not逻辑
 * @author anwen
 */
public interface Not<T, Children> {

    /**
     * 查询不匹配的文档
     * @param conditionMetaObject 条件元对象
     * @return children
     */
    Children not(ConditionMetaObject conditionMetaObject);

    /**
     * 查询不匹配的文档
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param conditionMetaObject 条件元对象
     * @return Children
     */
    Children not(boolean condition, ConditionMetaObject conditionMetaObject);

    /**
     * 查询不匹配的文档
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param queryChainWrapper 条件构造器
     * @return Children
     */
    Children not(boolean condition, QueryChainWrapper<?,?> queryChainWrapper);

    /**
     * 查询不匹配的文档
     * @param queryChainWrapper 条件构造器
     * @return Children
     */
    Children not(QueryChainWrapper<?,?> queryChainWrapper);

    /**
     * 查询不匹配的文档
     * @param function 条件构造器
     * @return Children
     */
    Children not(SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function);

    /**
     * 查询不匹配的文档
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param function 条件构造器
     * @return Children
     */
    Children not(boolean condition,SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function);

}
