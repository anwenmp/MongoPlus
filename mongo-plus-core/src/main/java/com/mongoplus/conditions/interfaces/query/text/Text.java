package com.mongoplus.conditions.interfaces.query.text;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.options.TextSearchOptions;

/**
 * 文本查询
 *
 * @author anwen
 * @mongodbOperator $text
 */
public interface Text<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 文本查询
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children text(boolean condition, Object value) {
        return condition ? text(value) : typeThis();
    }

    /**
     * 文本查询
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children text(Object value) {
        return text(value, null);
    }

    /**
     * 文本查询
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param value 值
     * @param textSearchOptions 搜索选项
     * @return {@link Children}
     * @author anwen
     */
    default Children text(boolean condition, Object value, TextSearchOptions textSearchOptions) {
        return condition ? text(value, textSearchOptions) : typeThis();
    }

    /**
     * 文本查询
     * @param value 值
     * @param textSearchOptions 搜索选项
     * @return {@link Children}
     * @author anwen
     */
    default Children text(Object value,TextSearchOptions textSearchOptions) {
        return addCondition(getBaseConditionExtraValue(value, textSearchOptions));
    }

}
