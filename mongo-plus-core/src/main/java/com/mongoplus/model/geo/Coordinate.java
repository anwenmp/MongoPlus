package com.mongoplus.model.geo;

public class Coordinate {

    /**
     * x坐标
     */
    private final Double x;

    /**
     * y坐标
     */
    private final Double y;

    public Coordinate(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

}
