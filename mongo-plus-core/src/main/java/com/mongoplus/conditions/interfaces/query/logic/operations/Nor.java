package com.mongoplus.conditions.interfaces.query.logic.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.support.SFunction;

/**
 * nor逻辑
 * @author anwen
 * @mongodbOperator $nor
 */
public interface Nor<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 查询的文档必须不符合所有条件
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param queryWrapper 实体对象封装操作类 {@link com.mongoplus.conditions.query.QueryWrapper}
     * @return Children
     * @author JiaChaoYang
     */
    default Children nor(boolean condition , Wrapper<?> queryWrapper) {
        return condition ? nor(queryWrapper) : typeThis();
    }

    /**
     * 查询的文档必须不符合所有条件
     * @param queryWrapper 实体对象封装操作类 {@link com.mongoplus.conditions.query.QueryWrapper}
     * @return Children
     * @author JiaChaoYang
     */
    default Children nor(Wrapper<?> queryWrapper) {
        return addCondition(getBaseCondition(queryWrapper));
    }

    /**
     * 查询的文档必须不符合所有条件
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param function 链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    default Children nor(boolean condition, SFunction<QueryWrapper<T>, QueryWrapper<T>> function) {
        return condition ? nor(function) : typeThis();
    }


    /**
     * 查询的文档必须不符合所有条件
     * @param function 链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    default Children nor(SFunction<QueryWrapper<T>, QueryWrapper<T>> function) {
        return nor(function.apply(new QueryWrapper<>()));
    }

}
