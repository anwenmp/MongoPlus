package com.mongoplus.conditions.interfaces.query.geo.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.model.geo.GeoCenter;
import com.mongoplus.support.SFunction;

/**
 * geoWithinCenter操作
 *
 * @author anwen
 */
public interface GeoWithinCenter<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * $center操作符符为$geoWithin查询指定一个圆
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param x 圆的x坐标
     * @param y 圆的y坐标
     * @param radius 以坐标系使用的单位测量的圆的半径
     * @return {@link Children}
     * @author anwen
     */
    default Children geoWithinCenter(boolean condition,String fieldName,double x, double y, double radius) {
        return condition ? geoWithinCenter(fieldName, x, y, radius) : typeThis();
    }

    /**
     * $center操作符符为$geoWithin查询指定一个圆
     * @param fieldName 字段名
     * @param x 圆的x坐标
     * @param y 圆的y坐标
     * @param radius 以坐标系使用的单位测量的圆的半径
     * @return {@link Children}
     * @author anwen
     */
    default Children geoWithinCenter(String fieldName,double x, double y, double radius) {
        return addCondition(getBaseCondition(fieldName, new GeoCenter(x,y,radius)));
    }

    /**
     * $center操作符符为$geoWithin查询指定一个圆
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param x 圆的x坐标
     * @param y 圆的y坐标
     * @param radius 以坐标系使用的单位测量的圆的半径
     * @return {@link Children}
     * @author anwen
     */
    default Children geoWithinCenter(boolean condition, SFunction<T,?> fieldName, double x, double y, double radius) {
        return condition ? geoWithinCenter(fieldName, x, y, radius) : typeThis();
    }

    /**
     * $center操作符符为$geoWithin查询指定一个圆
     * @param fieldName 字段名
     * @param x 圆的x坐标
     * @param y 圆的y坐标
     * @param radius 以坐标系使用的单位测量的圆的半径
     * @return {@link Children}
     * @author anwen
     */
    default Children geoWithinCenter(SFunction<T,?> fieldName,double x, double y, double radius) {
        return addCondition(getBaseCondition(fieldName, new GeoCenter(x,y,radius)));
    }

}
