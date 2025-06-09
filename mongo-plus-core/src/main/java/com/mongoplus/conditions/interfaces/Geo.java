package com.mongoplus.conditions.interfaces;

import com.mongodb.client.model.geojson.Geometry;
import com.mongodb.client.model.geojson.Point;
import com.mongoplus.annotation.comm.Nullable;
import com.mongoplus.support.SFunction;
import org.bson.conversions.Bson;

import java.io.Serializable;
import java.util.List;

/**
 * 地理空间查询
 * @author anwen
 */
@SuppressWarnings("unused")
public interface Geo<T,Children> extends Serializable {

    /**
     * 选择地理空间数据与指定 GeoJSON 对象相交的文档；
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    Children geoIntersects(boolean condition,String fieldName, Geometry geometry);

    /**
     * 选择地理空间数据与指定 GeoJSON 对象相交的文档；
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    Children geoIntersects(String fieldName, Geometry geometry);

    /**
     * 选择地理空间数据与指定 GeoJSON 对象相交的文档；
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    Children geoIntersects(boolean condition, SFunction<T,?> fieldName, Geometry geometry);

    /**
     * 选择地理空间数据与指定 GeoJSON 对象相交的文档；
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    Children geoIntersects(SFunction<T,?> fieldName, Geometry geometry);

    /**
     * 选择地理空间数据与指定 GeoJSON 对象相交的文档；
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    Children geoIntersects(boolean condition,String fieldName, Bson geometry);

    /**
     * 选择地理空间数据与指定 GeoJSON 对象相交的文档；
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    Children geoIntersects(String fieldName, Bson geometry);

    /**
     * 选择地理空间数据与指定 GeoJSON 对象相交的文档；
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    Children geoIntersects(boolean condition,SFunction<T,?> fieldName, Bson geometry);

    /**
     * 选择地理空间数据与指定 GeoJSON 对象相交的文档；
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    Children geoIntersects(SFunction<T,?> fieldName, Bson geometry);

    /**
     * 选择地理空间数据完全位于指定形状内的文档
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    Children geoWithin(boolean condition,String fieldName, Geometry geometry);

    /**
     * 选择地理空间数据完全位于指定形状内的文档
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    Children geoWithin(String fieldName, Geometry geometry);

    /**
     * 选择地理空间数据完全位于指定形状内的文档
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    Children geoWithin(boolean condition,SFunction<T,?> fieldName, Geometry geometry);

    /**
     * 选择地理空间数据完全位于指定形状内的文档
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    Children geoWithin(SFunction<T,?> fieldName, Geometry geometry);

    /**
     * 选择地理空间数据完全位于指定形状内的文档
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    Children geoWithin(boolean condition,String fieldName, Bson geometry);

    /**
     * 选择地理空间数据完全位于指定形状内的文档
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    Children geoWithin(String fieldName, Bson geometry);

    /**
     * 选择地理空间数据完全位于指定形状内的文档
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    Children geoWithin(boolean condition,SFunction<T,?> fieldName, Bson geometry);

    /**
     * 选择地理空间数据完全位于指定形状内的文档
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @return {@link Children}
     * @author anwen
     */
    Children geoWithin(SFunction<T,?> fieldName, Bson geometry);

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
    Children near(boolean condition, String fieldName, Point geometry, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

    /**
     * 指定地理空间查询要按从最近到最远的顺序为其返回文档的点
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @param maxDistance 距离中心点最多指定距离
     * @param minDistance 与中心点至少相距指定距离
     * @return {@link Children}
     * @author anwen
     */
    Children near(String fieldName, Point geometry, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

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
    Children near(boolean condition,SFunction<T,?> fieldName, Point geometry, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

    /**
     * 指定地理空间查询要按从最近到最远的顺序为其返回文档的点
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @param maxDistance 距离中心点最多指定距离
     * @param minDistance 与中心点至少相距指定距离
     * @return {@link Children}
     * @author anwen
     */
    Children near(SFunction<T,?> fieldName, Point geometry, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

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
    Children near(boolean condition,String fieldName, Bson geometry, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

    /**
     * 指定地理空间查询要按从最近到最远的顺序为其返回文档的点
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @param maxDistance 距离中心点最多指定距离
     * @param minDistance 与中心点至少相距指定距离
     * @return {@link Children}
     * @author anwen
     */
    Children near(String fieldName, Bson geometry, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

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
    Children near(boolean condition,SFunction<T,?> fieldName, Bson geometry, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

    /**
     * 指定地理空间查询要按从最近到最远的顺序为其返回文档的点
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @param maxDistance 距离中心点最多指定距离
     * @param minDistance 与中心点至少相距指定距离
     * @return {@link Children}
     * @author anwen
     */
    Children near(SFunction<T,?> fieldName, Bson geometry, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

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
    Children near(boolean condition,String fieldName, double x, double y, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

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
    Children near(String fieldName, double x, double y, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

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
    Children near(boolean condition,SFunction<T,?> fieldName, double x, double y, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

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
    Children near(SFunction<T,?> fieldName, double x, double y, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

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
    Children nearSphere(boolean condition, String fieldName, Point geometry, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

    /**
     * 指定地理空间查询要按从最近到最远的顺序为其返回文档的点
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @param maxDistance 距离中心点最多指定距离
     * @param minDistance 与中心点至少相距指定距离
     * @return {@link Children}
     * @author anwen
     */
    Children nearSphere(String fieldName, Point geometry, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

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
    Children nearSphere(boolean condition,SFunction<T,?> fieldName, Point geometry, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

    /**
     * 指定地理空间查询要按从最近到最远的顺序为其返回文档的点
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @param maxDistance 距离中心点最多指定距离
     * @param minDistance 与中心点至少相距指定距离
     * @return {@link Children}
     * @author anwen
     */
    Children nearSphere(SFunction<T,?> fieldName, Point geometry, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

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
    Children nearSphere(boolean condition,String fieldName, Bson geometry, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

    /**
     * 指定地理空间查询要按从最近到最远的顺序为其返回文档的点
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @param maxDistance 距离中心点最多指定距离
     * @param minDistance 与中心点至少相距指定距离
     * @return {@link Children}
     * @author anwen
     */
    Children nearSphere(String fieldName, Bson geometry, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

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
    Children nearSphere(boolean condition,SFunction<T,?> fieldName, Bson geometry, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

    /**
     * 指定地理空间查询要按从最近到最远的顺序为其返回文档的点
     * @param fieldName 字段名
     * @param geometry GeoJSON对象
     * @param maxDistance 距离中心点最多指定距离
     * @param minDistance 与中心点至少相距指定距离
     * @return {@link Children}
     * @author anwen
     */
    Children nearSphere(SFunction<T,?> fieldName, Bson geometry, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

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
    Children nearSphere(boolean condition,String fieldName, double x, double y, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

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
    Children nearSphere(String fieldName, double x, double y, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

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
    Children nearSphere(boolean condition,SFunction<T,?> fieldName, double x, double y, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

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
    Children nearSphere(SFunction<T,?> fieldName, double x, double y, @Nullable Double maxDistance,
                  @Nullable Double minDistance);

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
    Children geoWithinBox(boolean condition,String fieldName,double lowerLeftX, double lowerLeftY, double upperRightX,
                          double upperRightY);

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
    Children geoWithinBox(String fieldName,double lowerLeftX, double lowerLeftY, double upperRightX, 
                          double upperRightY);

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
    Children geoWithinBox(boolean condition,SFunction<T,?> fieldName,double lowerLeftX, double lowerLeftY, double upperRightX,
                          double upperRightY);

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
    Children geoWithinBox(SFunction<T,?> fieldName,double lowerLeftX, double lowerLeftY, double upperRightX,
                          double upperRightY);

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
    Children geoWithinCenter(boolean condition,String fieldName,double x, double y, double radius);
    
    /**
     * $center操作符符为$geoWithin查询指定一个圆
     * @param fieldName 字段名
     * @param x 圆的x坐标
     * @param y 圆的y坐标
     * @param radius 以坐标系使用的单位测量的圆的半径
     * @return {@link Children}
     * @author anwen
     */
    Children geoWithinCenter(String fieldName,double x, double y, double radius);

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
    Children geoWithinCenter(boolean condition,SFunction<T,?> fieldName,double x, double y, double radius);

    /**
     * $center操作符符为$geoWithin查询指定一个圆
     * @param fieldName 字段名
     * @param x 圆的x坐标
     * @param y 圆的y坐标
     * @param radius 以坐标系使用的单位测量的圆的半径
     * @return {@link Children}
     * @author anwen
     */
    Children geoWithinCenter(SFunction<T,?> fieldName,double x, double y, double radius);

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
    Children geoWithinCenterSphere(boolean condition,String fieldName,double x, double y, double radius);

    /**
     * $center操作符符为$geoWithin查询指定一个圆
     * @param fieldName 字段名
     * @param x 圆的x坐标
     * @param y 圆的y坐标
     * @param radius 以坐标系使用的单位测量的圆的半径
     * @return {@link Children}
     * @author anwen
     */
    Children geoWithinCenterSphere(String fieldName,double x, double y, double radius);

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
    Children geoWithinCenterSphere(boolean condition,SFunction<T,?> fieldName,double x, double y, double radius);

    /**
     * $center操作符符为$geoWithin查询指定一个圆
     * @param fieldName 字段名
     * @param x 圆的x坐标
     * @param y 圆的y坐标
     * @param radius 以坐标系使用的单位测量的圆的半径
     * @return {@link Children}
     * @author anwen
     */
    Children geoWithinCenterSphere(SFunction<T,?> fieldName,double x, double y, double radius);

    /**
     * 为传统坐标对上的地理空间 $geoWithin 查询指定多边形
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param points x、y 坐标对的列表
     * @return {@link Children}
     * @author anwen
     */
    Children geoWithinPolygon(boolean condition,String fieldName,List<List<Double>> points);

    /**
     * 为传统坐标对上的地理空间 $geoWithin 查询指定多边形
     * @param fieldName 字段名
     * @param points x、y 坐标对的列表
     * @return {@link Children}
     * @author anwen
     */
    Children geoWithinPolygon(String fieldName,List<List<Double>> points);

    /**
     * 为传统坐标对上的地理空间 $geoWithin 查询指定多边形
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param points x、y 坐标对的列表
     * @return {@link Children}
     * @author anwen
     */
    Children geoWithinPolygon(boolean condition,SFunction<T,?> fieldName,List<List<Double>> points);

    /**
     * 为传统坐标对上的地理空间 $geoWithin 查询指定多边形
     * @param fieldName 字段名
     * @param points x、y 坐标对的列表
     * @return {@link Children}
     * @author anwen
     */
    Children geoWithinPolygon(SFunction<T,?> fieldName,List<List<Double>> points);

}
