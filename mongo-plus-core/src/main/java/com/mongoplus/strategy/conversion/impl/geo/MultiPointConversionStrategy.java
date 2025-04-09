package com.mongoplus.strategy.conversion.impl.geo;

import com.mongodb.client.model.geojson.MultiPoint;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.Document;

import static com.mongoplus.strategy.conversion.impl.geo.GeometryUtil.getCrs;
import static com.mongoplus.strategy.conversion.impl.geo.GeometryUtil.getPosition;

/**
 * MultiPoint类型转换
 *
 * @author anwen
 */
public class MultiPointConversionStrategy implements ConversionStrategy<MultiPoint> {
    @Override
    public MultiPoint convertValue(Object fieldValue, Class<?> fieldType,
                                   MongoConverter mongoConverter) throws IllegalAccessException {
        return new MultiPoint(getCrs((Document) fieldValue),getPosition(fieldValue));
    }


}
