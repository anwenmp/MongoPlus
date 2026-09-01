package com.mongoplus.conditions.interfaces.query.geo.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.model.geo.GeoBox;
import com.mongoplus.support.SFunction;

/**
 * geoWithinBox操作
 * @author anwen
 * @mongodbOperator $geoWithin
 * @mongodbOperator $box
 */
public interface GeoWithinBox<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 为地理空间$geoWithin查询指定一个矩形，以根据基于点的位置数据返回矩形边界内的文档。
     * 与$box操作符一起使用时， $geoWithin根据网格坐标返回文档，并且不查询GeoJSON形状
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param lowerLeftX 矩形的左下角x坐标
     * @param lowerLeftY 矩形的左下角y坐标
     * @param upperRightX 矩形的右上角x坐标
     * @param upperRightY 矩形的右上角y坐标
     * @return {@link Children}
     * @author anwen
     */
    default Children geoWithinBox(boolean condition,String fieldName,double lowerLeftX, double lowerLeftY, double upperRightX,
                          double upperRightY) {
        return condition ? geoWithinBox(fieldName,lowerLeftX, lowerLeftY, upperRightX, upperRightY) : typeThis();
    }

    /**
     * 为地理空间$geoWithin查询指定一个矩形，以根据基于点的位置数据返回矩形边界内的文档。
     * 与$box操作符一起使用时， $geoWithin根据网格坐标返回文档，并且不查询GeoJSON形状
     * @param fieldName 字段名
     * @param lowerLeftX 矩形的左下角x坐标
     * @param lowerLeftY 矩形的左下角y坐标
     * @param upperRightX 矩形的右上角x坐标
     * @param upperRightY 矩形的右上角y坐标
     * @return {@link Children}
     * @author anwen
     */
    default Children geoWithinBox(String fieldName,double lowerLeftX, double lowerLeftY, double upperRightX,
                          double upperRightY) {
        return addCondition(getBaseCondition(fieldName, new GeoBox(lowerLeftX,lowerLeftY,upperRightX,upperRightY)));
    }

    /**
     * 为地理空间$geoWithin查询指定一个矩形，以根据基于点的位置数据返回矩形边界内的文档。
     * 与$box操作符一起使用时， $geoWithin根据网格坐标返回文档，并且不查询GeoJSON形状
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param lowerLeftX 矩形的左下角x坐标
     * @param lowerLeftY 矩形的左下角y坐标
     * @param upperRightX 矩形的右上角x坐标
     * @param upperRightY 矩形的右上角y坐标
     * @return {@link Children}
     * @author anwen
     */
    default Children geoWithinBox(boolean condition, SFunction<T,?> fieldName, double lowerLeftX, double lowerLeftY, double upperRightX,
                          double upperRightY) {
        return condition ? geoWithinBox(fieldName,lowerLeftX, lowerLeftY, upperRightX, upperRightY) : typeThis();
    }

    /**
     * 为地理空间$geoWithin查询指定一个矩形，以根据基于点的位置数据返回矩形边界内的文档。
     * 与$box操作符一起使用时， $geoWithin根据网格坐标返回文档，并且不查询GeoJSON形状
     * @param fieldName 字段名
     * @param lowerLeftX 矩形的左下角x坐标
     * @param lowerLeftY 矩形的左下角y坐标
     * @param upperRightX 矩形的右上角x坐标
     * @param upperRightY 矩形的右上角y坐标
     * @return {@link Children}
     * @author anwen
     */
    default Children geoWithinBox(SFunction<T,?> fieldName,double lowerLeftX, double lowerLeftY, double upperRightX,
                          double upperRightY) {
        return addCondition(getBaseCondition(fieldName, new GeoBox(lowerLeftX,lowerLeftY,upperRightX,upperRightY)));
    }

}
