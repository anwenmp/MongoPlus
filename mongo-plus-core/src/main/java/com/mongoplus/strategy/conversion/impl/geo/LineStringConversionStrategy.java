package com.mongoplus.strategy.conversion.impl.geo;

import com.mongodb.client.model.geojson.LineString;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.Document;

import static com.mongoplus.strategy.conversion.impl.geo.GeometryUtil.getCrs;
import static com.mongoplus.strategy.conversion.impl.geo.GeometryUtil.getPosition;

/**
 * LineString类型转换策略
 * @author anwen
 */
@SuppressWarnings("unchecked")
public class LineStringConversionStrategy implements ConversionStrategy<LineString> {
    @Override
    public LineString convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        return new LineString(getCrs((Document) fieldValue),getPosition(fieldValue));
    }
}
