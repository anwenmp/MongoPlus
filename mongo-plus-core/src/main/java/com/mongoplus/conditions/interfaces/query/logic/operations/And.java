package com.mongoplus.conditions.interfaces.query.logic.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.support.SFunction;

/**
 * and逻辑
 * @author anwen
 * @mongodbOperator $and
 */
public interface And<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * and
     * @param queryWrapper 实体对象封装操作类 {@link com.mongoplus.conditions.query.QueryWrapper}
     * @return Children
     * @author JiaChaoYang
     */
    default Children and(Wrapper<?> queryWrapper) {
        return addCondition(getBaseCondition(queryWrapper));
    }

    /**
     * and
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param queryWrapper 实体对象封装操作类 {@link com.mongoplus.conditions.query.QueryWrapper}
     * @return Children
     * @author JiaChaoYang
     */
    default Children and(boolean condition,Wrapper<?> queryWrapper) {
        return condition ? addCondition(getBaseCondition(queryWrapper)) : typeThis();
    }

    /**
     * and
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param function 链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    default Children and(boolean condition, SFunction<QueryWrapper<T>, QueryWrapper<T>> function) {
        return condition ? addCondition(getBaseCondition(function)) : typeThis();
    }


    /**
     * and
     * @param function 链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    default Children and(SFunction<QueryWrapper<T>, QueryWrapper<T>> function) {
        return and(function.apply(new QueryWrapper<>()));
    }

}
