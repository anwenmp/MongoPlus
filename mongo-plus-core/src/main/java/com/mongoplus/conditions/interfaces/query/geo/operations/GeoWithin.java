package com.mongoplus.conditions.interfaces.query.geo.operations;

import com.mongodb.client.model.geojson.Geometry;
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;
import org.bson.conversions.Bson;

/**
 * geoWithin操作
 * @author anwen
 * @mongodbOperator $geoWithin
 */
public interface GeoWithin<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 选择地理空间数据完全位于指定形状内的文档
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    default Children geoWithin(boolean condition,String fieldName, Geometry geometry) {
        return condition ? geoWithin(fieldName, geometry) : typeThis();
    }

    /**
     * 选择地理空间数据完全位于指定形状内的文档
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    default Children geoWithin(String fieldName, Geometry geometry) {
        return addCondition(getBaseCondition(fieldName, geometry));
    }

    /**
     * 选择地理空间数据完全位于指定形状内的文档
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    default Children geoWithin(boolean condition, SFunction<T,?> fieldName, Geometry geometry) {
        return condition ? geoWithin(fieldName, geometry) : typeThis();
    }

    /**
     * 选择地理空间数据完全位于指定形状内的文档
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    default Children geoWithin(SFunction<T,?> fieldName, Geometry geometry) {
        return addCondition(getBaseCondition(fieldName, geometry));
    }

    /**
     * 选择地理空间数据完全位于指定形状内的文档
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    default Children geoWithin(boolean condition,String fieldName, Bson geometry) {
        return condition ? geoWithin(fieldName, geometry) : typeThis();
    }

    /**
     * 选择地理空间数据完全位于指定形状内的文档
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    default Children geoWithin(String fieldName, Bson geometry) {
        return addCondition(getBaseCondition(fieldName, geometry));
    }

    /**
     * 选择地理空间数据完全位于指定形状内的文档
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    default Children geoWithin(boolean condition,SFunction<T,?> fieldName, Bson geometry) {
        return condition ? geoWithin(fieldName, geometry) : typeThis();
    }

    /**
     * 选择地理空间数据完全位于指定形状内的文档
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    default Children geoWithin(SFunction<T,?> fieldName, Bson geometry) {
        return addCondition(getBaseCondition(fieldName, geometry));
    }

}
