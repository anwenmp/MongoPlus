package com.mongoplus.conditions.interfaces.query.order;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.enums.OrderEnum;
import com.mongoplus.model.Order;
import com.mongoplus.support.SFunction;

/**
 * order操作
 *
 * @author anwen
 */
public interface Ordered<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 自定义排序
     * @param column 字段
     * @param order 排序方式
     * @return {@link Children}
     * @author anwen
     */
    default Children order(SFunction<T,Object> column, Integer order) {
        return order(column.getFieldNameLine(), order);
    }

    /**
     * 自定义排序
     * @param column 字段
     * @return {@link Children}
     * @author anwen
     */
    default Children order(String column,Integer order) {
        return addCondition(new Order(column, order));
    }

    /**
     * 正序排序
     * @param column 列名、字段名，lambda方式
     * @return com.mongoplus.sql.query.LambdaQueryMongoWrapper<T>
     * @author JiaChaoYang
     */
    default Children orderByAsc(SFunction<T, Object> column) {
        return orderByAsc(column.getFieldNameLine());
    }

    /**
     * 倒序排序
     * @param column 列名、字段名，lambda方式
     * @return com.mongoplus.sql.query.LambdaQueryMongoWrapper<T>
     * @author JiaChaoYang
     */
    default Children orderByDesc(SFunction<T,Object> column) {
        return orderByDesc(column.getFieldNameLine());
    }

    /**
     * 正序排序
     * @param column 列名、字段名
     * @return com.mongoplus.sql.query.LambdaQueryMongoWrapper<T>
     * @author JiaChaoYang
     */
    default Children orderByAsc(String column) {
        return order(column, OrderEnum.ASC.getValue());
    }

    /**
     * 倒序排序
     * @param column 列名、字段名，lambda方式
     * @return com.mongoplus.sql.query.LambdaQueryMongoWrapper<T>
     * @author JiaChaoYang
     */
    default Children orderByDesc(String column) {
        return order(column, OrderEnum.DESC.getValue());
    }

}
