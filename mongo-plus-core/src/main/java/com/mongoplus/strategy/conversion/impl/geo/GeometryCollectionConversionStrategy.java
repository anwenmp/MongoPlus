package com.mongoplus.strategy.conversion.impl.geo;

import com.mongodb.client.model.geojson.Geometry;
import com.mongodb.client.model.geojson.GeometryCollection;
import com.mongoplus.cache.global.ConversionCache;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.Document;

import java.util.LinkedList;
import java.util.List;

import static com.mongoplus.strategy.conversion.impl.geo.GeometryUtil.getCrs;

/**
 * GeometryCollection类型转换策略
 * @author anwen
 */
public class GeometryCollectionConversionStrategy implements ConversionStrategy<GeometryCollection> {

    @Override
    @SuppressWarnings("unchecked")
    public GeometryCollection convertValue(Object fieldValue, Class<?> fieldType,
                                           MongoConverter mongoConverter) throws IllegalAccessException {
        Document document = (Document) fieldValue;
        List<Geometry> geometrieList = new LinkedList<>();
        List<Document> geometries = document.getList("geometries", Document.class);
        ConversionStrategy<Geometry> conversionStrategy =
                (ConversionStrategy<Geometry>) ConversionCache.getConversionStrategy(Geometry.class);
        geometries.forEach(geometrie -> {
            try {
                geometrieList.add(conversionStrategy.convertValue(geometrie,fieldType,mongoConverter));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        return new GeometryCollection(getCrs(document),geometrieList);
    }
}
