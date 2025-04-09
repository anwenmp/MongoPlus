package com.mongoplus.model.geo;

import com.mongodb.client.model.geojson.Point;
import com.mongoplus.toolkit.ClassTypeUtil;
import com.mongoplus.toolkit.Filters;
import org.bson.conversions.Bson;

public class GeoNear {

    /**
     * GeoJSON对象
     */
    private final Object geometry;

    /**
     * 距离中心点最多指定距离
     */
    private final Double maxDistance;

    /**
     * 与中心点至少相距指定距离
     */
    private final Double minDistance;

    public GeoNear(Object geometry, Double maxDistance, Double minDistance) {
        this.geometry = geometry;
        this.maxDistance = maxDistance;
        this.minDistance = minDistance;
    }

    public Bson buildNear(String fieldName) {
        Class<?> clazz = this.geometry.getClass();
        if (ClassTypeUtil.isTargetClass(Point.class,clazz)) {
            return Filters.near(fieldName,(Point) this.geometry,this.maxDistance,this.minDistance);
        } else if (ClassTypeUtil.isTargetClass(Coordinate.class, clazz)) {
            Coordinate coordinate = (Coordinate) geometry;
            return Filters.near(fieldName,coordinate.getX(),coordinate.getY(),this.maxDistance,this.minDistance);
        } else {
            return Filters.near(fieldName,(Bson) this.geometry,this.maxDistance,this.minDistance);
        }
    }

    public Bson buildNearSphere(String fieldName) {
        Class<?> clazz = this.geometry.getClass();
        if (ClassTypeUtil.isTargetClass(Point.class,clazz)) {
            return Filters.nearSphere(fieldName,(Point) this.geometry,this.maxDistance,this.minDistance);
        } else if (ClassTypeUtil.isTargetClass(Coordinate.class, clazz)) {
            Coordinate coordinate = (Coordinate) geometry;
            return Filters.nearSphere(fieldName,coordinate.getX(),coordinate.getY(),this.maxDistance,this.minDistance);
        } else {
            return Filters.nearSphere(fieldName,(Bson) this.geometry,this.maxDistance,this.minDistance);
        }
    }

    public Object getGeometry() {
        return geometry;
    }

    public Double getMaxDistance() {
        return maxDistance;
    }

    public Double getMinDistance() {
        return minDistance;
    }
}
