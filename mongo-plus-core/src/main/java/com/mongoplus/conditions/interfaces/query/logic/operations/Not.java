package com.mongoplus.conditions.interfaces.query.logic.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.support.SFunction;

import java.util.Collections;

import static com.mongoplus.handlers.condition.BuildCondition.condition;

/**
 * not逻辑
 * @author anwen
 * @mongodbOperator $not
 * @mongodbOperator $nor
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
     * @param queryWrapper 实体对象封装操作类 {@link com.mongoplus.conditions.query.QueryWrapper}
     * @return Children
     */
    default Children not(boolean condition, Wrapper<?> queryWrapper) {
        return condition ? not(queryWrapper) : typeThis();
    }

    /**
     * 查询不匹配的文档
     * @param queryWrapper 实体对象封装操作类 {@link com.mongoplus.conditions.query.QueryWrapper}
     * @return Children
     */
    default Children not(Wrapper<?> queryWrapper) {
        return addCondition(getBaseCondition(queryWrapper));
    }

    /**
     * 查询不匹配的文档
     * @param function 条件构造器
     * @return Children
     */
    default Children not(SFunction<QueryWrapper<T>, QueryWrapper<T>> function) {
        return not(function.apply(new QueryWrapper<>()));
    }

    /**
     * 查询不匹配的文档
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param function 条件构造器
     * @return Children
     */
    default Children not(boolean condition, SFunction<QueryWrapper<T>, QueryWrapper<T>> function) {
        return condition ? not(function) : typeThis();
    }

}
