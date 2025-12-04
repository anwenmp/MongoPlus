package com.mongoplus.conditions.interfaces.query.other.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;

/**
 * where操作
 *
 * @author anwen
 */
public interface Where<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 匹配给定表达式为 true 的所有文档
     * @param javaScriptExpression JavaScript 表达式
     * @return {@link Children}
     * @author anwen
     */
    default Children where(String javaScriptExpression) {
        return addCondition(getBaseCondition(javaScriptExpression));
    }

}
