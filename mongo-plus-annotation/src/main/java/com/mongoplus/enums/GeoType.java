package com.mongoplus.enums;

/**
 * 地理位置索引类型
 * @author anwen
 */
public enum GeoType {

    /**
     * 2dsphere索引
     */
    SPHERE("2dsphere"),

    /**
     * 2d索引
     */
    TWO_D("2d");

    private final String type;

    GeoType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
