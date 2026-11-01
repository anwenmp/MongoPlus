package com.mongoplus.conditions.interfaces.query.other.operations;

import com.mongodb.BasicDBObject;
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.enums.QueryOperatorEnum;
import com.mongoplus.support.SFunction;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static com.mongoplus.handlers.condition.BuildCondition.condition;

/**
 * expr操作
 *
 * @author anwen
 * @mongodbOperator $expr
 */
public interface Expr<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 进行计算的表达式
     * 条件元对象支持 eq/ne/gt/gte/lt/lte 的字段与常量比较；任意表达式使用 Wrapper 重载。
     * @author JiaChaoYang
     */
    default Children expr(ConditionMetaObject conditionMetaObject) {
        BasicDBObject query = condition().queryCondition(Collections.singletonList(conditionMetaObject));
        QueryOperatorEnum operator = QueryOperatorEnum.getQueryOperator(conditionMetaObject.getCondition());
        if (!Arrays.asList(QueryOperatorEnum.EQ, QueryOperatorEnum.NE, QueryOperatorEnum.GT,
                QueryOperatorEnum.GTE, QueryOperatorEnum.LT, QueryOperatorEnum.LTE).contains(operator)) {
            throw new IllegalArgumentException("expr(ConditionMetaObject) requires a comparison condition");
        }
        String column = conditionMetaObject.getColumn();
        Map<?, ?> comparison = (Map<?, ?>) query.get(column);
        // Query values remain literal, including strings starting with '$' and embedded documents.
        BasicDBObject expression = new BasicDBObject(operator.getOperatorValue(),
                Arrays.asList("$" + column, new BasicDBObject("$literal", comparison.get(operator.getOperatorValue()))));
        return addCondition(getBaseCondition(expression));
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
