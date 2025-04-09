package com.mongoplus.strategy.conversion.impl.geo;

import com.mongodb.client.model.geojson.MultiLineString;
import com.mongodb.client.model.geojson.Position;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.Document;

import java.util.LinkedList;
import java.util.List;

import static com.mongoplus.strategy.conversion.impl.geo.GeometryUtil.getCrs;

/**
 * MultiLineString类型转换
 * @author anwen
 */
@SuppressWarnings("unchecked")
public class MultiLineStringConversionStrategy implements ConversionStrategy<MultiLineString> {

    @Override
    public MultiLineString convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        List<List<Position>> positions = new LinkedList<>();
        Document document = (Document) fieldValue;
        List<Object> coordinates = document.getList("coordinates", Object.class);
        coordinates.forEach(coordinate -> {
            List<Position> positionList = new LinkedList<>();
            coordinates.forEach(inner -> positionList.add(new Position((List<Double>) inner)));
            positions.add(positionList);
        });
        return new MultiLineString(getCrs(document),positions);
    }
}
