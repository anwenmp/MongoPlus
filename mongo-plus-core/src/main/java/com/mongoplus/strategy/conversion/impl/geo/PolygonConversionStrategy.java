/*
 * Copyright (c) JiaChaoYang 2025-4-9 MongoPlus版权所有
 * 适度编码益脑，沉迷编码伤身，合理安排时间，享受快乐生活。
 * email: j15030047216@163.com
 * phone: 15030047216
 * weChat: JiaChaoYang_
 */

package com.mongoplus.strategy.conversion.impl.geo;

import com.mongodb.client.model.geojson.Polygon;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.Document;

import java.util.List;

import static com.mongoplus.strategy.conversion.impl.geo.GeometryUtil.getCrs;
import static com.mongoplus.strategy.conversion.impl.geo.GeometryUtil.getPolygonCoordinates;

/**
 * Polygon类型转换策略
 * @author anwen
 */
public class PolygonConversionStrategy implements ConversionStrategy<Polygon> {
    @Override
    public Polygon convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        Document document = (Document) fieldValue;
        List<Object> coordinates = document.getList("coordinates", Object.class);
        return new Polygon(getCrs(document),getPolygonCoordinates(coordinates));
    }
}
