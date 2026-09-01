package com.mongoplus.conditions.interfaces.query.geo.operations;

import com.mongodb.client.model.geojson.Point;
import com.mongoplus.annotation.comm.Nullable;
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.model.geo.Coordinate;
import com.mongoplus.model.geo.GeoNear;
import com.mongoplus.support.SFunction;
import org.bson.conversions.Bson;

/**
 * nearSphere操作
 * @author anwen
 * @mongodbOperator $nearSphere
 */
public interface NearSphere<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 指定地理空间查询要按从最近到最远的顺序为其返回文档的点
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @param maxDistance 距离中心点最多指定距离
     * @param minDistance 与中心点至少相距指定距离
     * @return {@link Children}
     * @author anwen
     */
    default Children nearSphere(boolean condition, String fieldName, Point geometry, @Nullable Double maxDistance,
                        @Nullable Double minDistance) {
        return condition ? nearSphere(fieldName,geometry,maxDistance,minDistance) : typeThis();
    }

    /**
     * 指定地理空间查询要按从最近到最远的顺序为其返回文档的点
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @param maxDistance 距离中心点最多指定距离
     * @param minDistance 与中心点至少相距指定距离
     * @return {@link Children}
     * @author anwen
     */
    default Children nearSphere(String fieldName, Point geometry, @Nullable Double maxDistance,
                        @Nullable Double minDistance) {
        return addCondition(getBaseCondition(
                Thread.currentThread().getStackTrace()[2].getMethodName(),
                fieldName,
                new GeoNear(geometry,maxDistance,minDistance),
                Object.class,
                null,
                null
        ));
    }

    /**
     * 指定地理空间查询要按从最近到最远的顺序为其返回文档的点
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @param maxDistance 距离中心点最多指定距离
     * @param minDistance 与中心点至少相距指定距离
     * @return {@link Children}
     * @author anwen
     */
    default Children nearSphere(boolean condition, SFunction<T,?> fieldName, Point geometry, @Nullable Double maxDistance,
                        @Nullable Double minDistance) {
        return condition ? nearSphere(fieldName,geometry,maxDistance,minDistance) : typeThis();
    }

    /**
     * 指定地理空间查询要按从最近到最远的顺序为其返回文档的点
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @param maxDistance 距离中心点最多指定距离
     * @param minDistance 与中心点至少相距指定距离
     * @return {@link Children}
     * @author anwen
     */
    default Children nearSphere(SFunction<T,?> fieldName, Point geometry, @Nullable Double maxDistance,
                        @Nullable Double minDistance) {
        return addCondition(getBaseCondition(
                Thread.currentThread().getStackTrace()[2].getMethodName(),
                fieldName.getFieldNameLine(),
                new GeoNear(geometry,maxDistance,minDistance),
                fieldName.getImplClass(),
                fieldName.getField(),
                null
        ));
    }

    /**
     * 指定地理空间查询要按从最近到最远的顺序为其返回文档的点
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @param maxDistance 距离中心点最多指定距离
     * @param minDistance 与中心点至少相距指定距离
     * @return {@link Children}
     * @author anwen
     */
    default Children nearSphere(boolean condition, String fieldName, Bson geometry, @Nullable Double maxDistance,
                        @Nullable Double minDistance) {
        return condition ? nearSphere(fieldName,geometry,maxDistance,minDistance) : typeThis();
    }

    /**
     * 指定地理空间查询要按从最近到最远的顺序为其返回文档的点
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @param maxDistance 距离中心点最多指定距离
     * @param minDistance 与中心点至少相距指定距离
     * @return {@link Children}
     * @author anwen
     */
    default Children nearSphere(String fieldName, Bson geometry, @Nullable Double maxDistance,
                        @Nullable Double minDistance) {
        return addCondition(getBaseCondition(
                Thread.currentThread().getStackTrace()[2].getMethodName(),
                fieldName,
                new GeoNear(geometry,maxDistance,minDistance),
                Object.class,
                null,
                null
        ));
    }

    /**
     * 指定地理空间查询要按从最近到最远的顺序为其返回文档的点
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @param maxDistance 距离中心点最多指定距离
     * @param minDistance 与中心点至少相距指定距离
     * @return {@link Children}
     * @author anwen
     */
    default Children nearSphere(boolean condition,SFunction<T,?> fieldName, Bson geometry, @Nullable Double maxDistance,
                        @Nullable Double minDistance) {
        return condition ? nearSphere(fieldName,geometry,maxDistance,minDistance) : typeThis();
    }

    /**
     * 指定地理空间查询要按从最近到最远的顺序为其返回文档的点
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @param maxDistance 距离中心点最多指定距离
     * @param minDistance 与中心点至少相距指定距离
     * @return {@link Children}
     * @author anwen
     */
    default Children nearSphere(SFunction<T,?> fieldName, Bson geometry, @Nullable Double maxDistance,
                        @Nullable Double minDistance) {
        return addCondition(getBaseCondition(
                Thread.currentThread().getStackTrace()[2].getMethodName(),
                fieldName.getFieldNameLine(),
                new GeoNear(geometry,maxDistance,minDistance),
                fieldName.getImplClass(),
                fieldName.getField(),
                null
        ));
    }

    /**
     * 指定地理空间查询要按从最近到最远的顺序为其返回文档的点
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param x x坐标
     * @param y y坐标
     * @param maxDistance 距离中心点最多指定距离
     * @param minDistance 与中心点至少相距指定距离
     * @return {@link Children}
     * @author anwen
     */
    default Children nearSphere(boolean condition,String fieldName, double x, double y, @Nullable Double maxDistance,
                        @Nullable Double minDistance) {
        return condition ? nearSphere(fieldName,x,y,maxDistance,minDistance) : typeThis();
    }

    /**
     * 指定地理空间查询要按从最近到最远的顺序为其返回文档的点
     * @param fieldName 字段名
     * @param x x坐标
     * @param y y坐标
     * @param maxDistance 距离中心点最多指定距离
     * @param minDistance 与中心点至少相距指定距离
     * @return {@link Children}
     * @author anwen
     */
    default Children nearSphere(String fieldName, double x, double y, @Nullable Double maxDistance,
                        @Nullable Double minDistance) {
        return addCondition(getBaseCondition(
                Thread.currentThread().getStackTrace()[2].getMethodName(),
                fieldName,
                new GeoNear(new Coordinate(x,y),maxDistance,minDistance),
                Object.class,
                null,
                null
        ));
    }

    /**
     * 指定地理空间查询要按从最近到最远的顺序为其返回文档的点
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param x x坐标
     * @param y y坐标
     * @param maxDistance 距离中心点最多指定距离
     * @param minDistance 与中心点至少相距指定距离
     * @return {@link Children}
     * @author anwen
     */
    default Children nearSphere(boolean condition,SFunction<T,?> fieldName, double x, double y, @Nullable Double maxDistance,
                        @Nullable Double minDistance) {
        return condition ? nearSphere(fieldName,x,y,maxDistance,minDistance) : typeThis();
    }

    /**
     * 指定地理空间查询要按从最近到最远的顺序为其返回文档的点
     * @param fieldName 字段名
     * @param x x坐标
     * @param y y坐标
     * @param maxDistance 距离中心点最多指定距离
     * @param minDistance 与中心点至少相距指定距离
     * @return {@link Children}
     * @author anwen
     */
    default Children nearSphere(SFunction<T,?> fieldName, double x, double y, @Nullable Double maxDistance,
                        @Nullable Double minDistance) {
        return addCondition(getBaseCondition(
                Thread.currentThread().getStackTrace()[2].getMethodName(),
                fieldName.getFieldNameLine(),
                new GeoNear(new Coordinate(x,y),maxDistance,minDistance),
                fieldName.getImplClass(),
                fieldName.getField(),
                null
        ));
    }

}
