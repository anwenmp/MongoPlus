package com.mongoplus.conditions.interfaces.query.logic.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.support.SFunction;

import java.util.Collections;

import static com.mongoplus.handlers.condition.BuildCondition.condition;

/**
 * not逻辑
 * @author anwen
 */
public interface Not<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 查询不匹配的文档
     * @param conditionMetaObject 条件元对象
     * @return children
     */
    default Children not(ConditionMetaObject conditionMetaObject) {
        return addCondition(getBaseCondition(
                Collections.singletonList(condition().queryCondition(conditionMetaObject))
        ));
    }

    /**
     * 查询不匹配的文档
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param conditionMetaObject 条件元对象
     * @return Children
     */
    default Children not(boolean condition, ConditionMetaObject conditionMetaObject) {
        return condition ? not(conditionMetaObject) : typeThis();
    }

    /**
     * 查询不匹配的文档
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param queryChainWrapper 条件构造器
     * @return Children
     */
    default Children not(boolean condition, QueryChainWrapper<?,?> queryChainWrapper) {
        return condition ? not(queryChainWrapper) : typeThis();
    }

    /**
     * 查询不匹配的文档
     * @param queryChainWrapper 条件构造器
     * @return Children
     */
    default Children not(QueryChainWrapper<?,?> queryChainWrapper) {
        return addCondition(getBaseCondition(queryChainWrapper));
    }

    /**
     * 查询不匹配的文档
     * @param function 条件构造器
     * @return Children
     */
    default Children not(SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function) {
        return not(function.apply(new QueryWrapper<>()));
    }

    /**
     * 查询不匹配的文档
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param function 条件构造器
     * @return Children
     */
    default Children not(boolean condition,SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function) {
        return condition ? not(function) : typeThis();
    }

}
