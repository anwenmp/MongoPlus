package com.mongoplus.strategy.conversion.impl.geo;

import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.Document;

import java.util.List;

import static com.mongoplus.strategy.conversion.impl.geo.GeometryUtil.getCrs;

/**
 * Point类型转换策略
 * @author anwen
 */
public class PointConversionStrategy implements ConversionStrategy<Point> {

    @Override
    public Point convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        Document document = (Document) fieldValue;
        List<Double> coordinates = document.getList("coordinates", Double.class);
        return new Point(getCrs(document),new Position(coordinates));
    }
}
