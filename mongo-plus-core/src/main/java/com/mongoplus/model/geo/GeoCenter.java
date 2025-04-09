package com.mongoplus.model.geo;

import com.mongoplus.toolkit.Filters;
import org.bson.conversions.Bson;

public class GeoCenter {

    /**
     * 圆的x坐标
     */
    private final Double x;

    /**
     * 圆的y坐标
     */
    private final Double y;

    /**
     * 以坐标系使用的单位测量的圆的半径
     */
    private final Double radius;

    public GeoCenter(Double x, Double y, Double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public Bson buildCenter(String fieldName) {
        return Filters.geoWithinCenter(fieldName,this.x,this.y,this.radius);
    }

    public Bson buildCenterSphere(String fieldName) {
        return Filters.geoWithinCenterSphere(fieldName,this.x,this.y,this.radius);
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public Double getRadius() {
        return radius;
    }
}
