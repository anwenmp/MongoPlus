package com.mongoplus.strategy.conversion.impl.geo;

import com.mongodb.client.model.geojson.Geometry;
import com.mongoplus.cache.global.ConversionCache;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.Document;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Geometry类型转换策略实现类
 * @author anwen
 */
public class GeometryConversionStrategy implements ConversionStrategy<Geometry> {

    final Map<String,ConversionStrategy<? extends Geometry>> geometryResources =
            ConversionCache.geometryConversionMap.entrySet().stream()
            .collect(Collectors.toMap(
                    e -> e.getKey().getSimpleName(),
                    Map.Entry::getValue
            ));;

    @Override
    public Geometry convertValue(Object fieldValue, Class<?> fieldType,
                                 MongoConverter mongoConverter) throws IllegalAccessException {
        Document document = (Document) fieldValue;
        String type = document.getString("type");
        ConversionStrategy<? extends Geometry> conversionStrategy = geometryResources.get(type);
        if (conversionStrategy == null) {
            return null;
        }
        return conversionStrategy.convertValue(fieldValue,fieldType,mongoConverter);
    }
}
