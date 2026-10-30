package com.mongoplus.conditions.interfaces.query.other.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.support.SFunction;

import com.mongoplus.enums.QueryOperatorEnum;
import org.bson.Document;

import java.util.Arrays;

/**
 * expr操作
 *
 * @author anwen
 * @mongodbOperator $expr
 */
public interface Expr<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 进行计算的表达式
     * @author JiaChaoYang
     */
    default Children expr(ConditionMetaObject conditionMetaObject) {
        String operator = QueryOperatorEnum.getQueryOperator(conditionMetaObject.getCondition()).getOperatorValue();
        return expr(new QueryWrapper<T>().custom(new Document(operator,
                Arrays.asList("$" + conditionMetaObject.getColumn(), conditionMetaObject.getValue()))));
    }

    /**
     * 进行计算的表达式
     * @author JiaChaoYang
     */
    default Children expr(boolean condition, ConditionMetaObject conditionMetaObject) {
        return condition ? expr(conditionMetaObject) : typeThis();
    }

    /**
     * 进行计算的表达式
     * @param queryWrapper 实体对象封装操作类 {@link com.mongoplus.conditions.query.QueryWrapper}
     * @author anwen
     */
    default Children expr(boolean condition, Wrapper<?> queryWrapper) {
        return condition ? expr(queryWrapper) : typeThis();
    }

    /**
     * 进行计算的表达式
     * @param queryWrapper 实体对象封装操作类 {@link com.mongoplus.conditions.query.QueryWrapper}
     * @author anwen
     */
    default Children expr(Wrapper<?> queryWrapper) {
        return addCondition(getBaseCondition(queryWrapper));
    }

    /**
     * 进行计算的表达式
     * @author anwen
     */
    default Children expr(SFunction<QueryWrapper<T>, QueryWrapper<T>> function) {
        return expr(function.apply(new QueryWrapper<>()));
    }

    /**
     * 进行计算的表达式
     * @author anwen
     */
    default Children expr(boolean condition, SFunction<QueryWrapper<T>, QueryWrapper<T>> function) {
        return condition ? expr(function) : typeThis();
    }

}
