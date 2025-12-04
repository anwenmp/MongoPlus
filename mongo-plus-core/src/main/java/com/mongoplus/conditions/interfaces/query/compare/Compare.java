package com.mongoplus.conditions.interfaces.query.compare;

import com.mongoplus.conditions.interfaces.query.compare.operations.*;
import com.mongoplus.support.SFunction;

/**
 * 比较条件
 * @author anwen
 */
public interface Compare<T, Children> extends
        Eq<T, Children>,
        Gt<T, Children>,
        Gte<T, Children>,
        In<T, Children>,
        Lt<T, Children>,
        Lte<T, Children>,
        Ne<T, Children>,
        Nin<T, Children> {

    /**
     * 在。。。之间
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param gte 大于等于
     * @param lte 小于等于
     * @param convertGtOrLt 设置为true，则转换为大于-小于，默认为大于等于和小于等于
     * @return Children
     * @author JiaChaoYang
     */
    default Children between(boolean condition , SFunction<T,Object> column, Object gte, Object lte,
                             boolean convertGtOrLt) {
        return condition ? between(column,gte,lte,convertGtOrLt) : typeThis();
    }

    /**
     * 在。。。之间
     * @param column 列名、字段名
     * @param gte 大于等于
     * @param lte 小于等于
     * @param convertGtOrLt 设置为true，则转换为大于-小于，默认为大于等于和小于等于
     * @return Children
     * @author JiaChaoYang
     */
    default Children between(SFunction<T,Object> column,Object gte,Object lte,boolean convertGtOrLt) {
        if (!convertGtOrLt) {
            gte(column, gte);
            lte(column, lte);
        } else {
            gt(column, gte);
            lt(column, lte);
        }
        return typeThis();
    }

    /**
     * 在。。。之间
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param gte 大于等于
     * @param lte 小于等于
     * @param convertGtOrLt 设置为true，则转换为大于-小于，默认为大于等于和小于等于
     * @return Children
     * @author JiaChaoYang
     */
    default Children between(boolean condition,String column,Object gte,Object lte,boolean convertGtOrLt) {
        return condition ? between(column,gte,lte,convertGtOrLt) : typeThis();
    }

    /**
     * 在。。。之间
     * @param column 列名、字段名
     * @param gte 大于等于
     * @param lte 小于等于
     * @param convertGtOrLt 设置为true，则转换为大于-小于，默认为大于等于和小于等于
     * @return Children
     * @author JiaChaoYang
     */
    default Children between(String column,Object gte,Object lte,boolean convertGtOrLt) {
        if (!convertGtOrLt) {
            gte(column, gte);
            lte(column, lte);
        } else {
            gt(column, gte);
            lt(column, lte);
        }
        return typeThis();
    }

}
