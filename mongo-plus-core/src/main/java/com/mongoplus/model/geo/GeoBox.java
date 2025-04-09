package com.mongoplus.model.geo;

import com.mongoplus.toolkit.Filters;
import org.bson.conversions.Bson;

public class GeoBox {

    /**
     * 矩形的左下角x坐标
     */
    private final Double lowerLeftX;

    /**
     * 矩形的左下角y坐标
     */
    private final Double lowerLeftY;

    /**
     * 矩形的右上角x坐标
     */
    private final Double upperRightX;

    /**
     * 矩形的右上角y坐标
     */
    private final Double upperRightY;

    public GeoBox(double lowerLeftX, double lowerLeftY, double upperRightX, double upperRightY) {
        this.lowerLeftX = lowerLeftX;
        this.lowerLeftY = lowerLeftY;
        this.upperRightX = upperRightX;
        this.upperRightY = upperRightY;
    }

    public Bson toBson(String fieldName) {
        return Filters.geoWithinBox(fieldName,this.lowerLeftX,this.lowerLeftY,this.upperRightX,this.upperRightY);
    }

    public double getLowerLeftX() {
        return lowerLeftX;
    }

    public double getLowerLeftY() {
        return lowerLeftY;
    }

    public double getUpperRightX() {
        return upperRightX;
    }

    public double getUpperRightY() {
        return upperRightY;
    }

    @Override
    public String toString() {
        return "GeoBox{" +
                "lowerLeftX=" + lowerLeftX +
                ", lowerLeftY=" + lowerLeftY +
                ", upperRightX=" + upperRightX +
                ", upperRightY=" + upperRightY +
                '}';
    }

}
