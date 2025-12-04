package com.mongoplus.conditions.interfaces.query.geo.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;

import java.util.List;

/**
 * geoWithinPolygon操作
 *
 * @author anwen
 */
public interface GeoWithinPolygon<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 为传统坐标对上的地理空间 $geoWithin 查询指定多边形
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param points x、y 坐标对的列表
     * @return {@link Children}
     * @author anwen
     */
    default Children geoWithinPolygon(boolean condition,String fieldName,List<List<Double>> points) {
        return condition ? geoWithinPolygon(fieldName,points) : typeThis();
    }

    /**
     * 为传统坐标对上的地理空间 $geoWithin 查询指定多边形
     * @param fieldName 字段名
     * @param points x、y 坐标对的列表
     * @return {@link Children}
     * @author anwen
     */
    default Children geoWithinPolygon(String fieldName,List<List<Double>> points) {
        return addCondition(getBaseCondition(fieldName, points));
    }

    /**
     * 为传统坐标对上的地理空间 $geoWithin 查询指定多边形
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param points x、y 坐标对的列表
     * @return {@link Children}
     * @author anwen
     */
    default Children geoWithinPolygon(boolean condition, SFunction<T,?> fieldName, List<List<Double>> points) {
        return condition ? geoWithinPolygon(fieldName,points) : typeThis();
    }

    /**
     * 为传统坐标对上的地理空间 $geoWithin 查询指定多边形
     * @param fieldName 字段名
     * @param points x、y 坐标对的列表
     * @return {@link Children}
     * @author anwen
     */
    default Children geoWithinPolygon(SFunction<T,?> fieldName,List<List<Double>> points) {
        return addCondition(getBaseCondition(fieldName, points));
    }

}
