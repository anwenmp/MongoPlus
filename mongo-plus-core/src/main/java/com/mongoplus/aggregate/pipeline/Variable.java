package com.mongoplus.aggregate.pipeline;

import com.mongoplus.support.SFunction;

/**
 * $lookup阶段中的新变量,let
 * 对{@link com.mongodb.client.model.Variable<TExpression>}进行封装，支持lambda
 * @author anwen
 */
public class Variable<TExpression> extends com.mongodb.client.model.Variable<TExpression> {

    /**
     * 创建一个新的变量定义，用于 $lookup 管道阶段
     * @param name 新变量的名称
     * @param value 新变量的值
     * @author anwen
     * @since mongodb.driver.manual reference/operator/aggregation/lookup/  $lookup
     */
    @SuppressWarnings("unchecked")
    public <T,R> Variable(String name, SFunction<T,R> value) {
        super(name, (TExpression) value.getFieldNameLineOption());
    }

    @SuppressWarnings("unchecked")
    public Variable(String name, String value) {
        super(name, (TExpression) value);
    }
}
