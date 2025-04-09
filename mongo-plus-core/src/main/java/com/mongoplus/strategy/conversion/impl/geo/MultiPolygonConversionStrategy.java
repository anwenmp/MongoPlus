package com.mongoplus.strategy.conversion.impl.geo;

import com.mongodb.client.model.geojson.MultiPolygon;
import com.mongodb.client.model.geojson.PolygonCoordinates;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.CollUtil;
import org.bson.Document;

import java.util.List;
import java.util.stream.Collectors;

import static com.mongoplus.strategy.conversion.impl.geo.GeometryUtil.getCrs;
import static com.mongoplus.strategy.conversion.impl.geo.GeometryUtil.getPolygonCoordinates;

/**
 * MultiPolygon类型转换策略
 * @author anwen
 */
public class MultiPolygonConversionStrategy implements ConversionStrategy<MultiPolygon> {

    @Override
    @SuppressWarnings("unchecked")
    public MultiPolygon convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        if (!(fieldValue instanceof Document)) {
            return null;
        }

        Document document = (Document) fieldValue;
        List<Object> coordinateObjects = document.getList("coordinates", Object.class);
        if (CollUtil.isEmpty(coordinateObjects)) {
            return null;
        }

        // 将外层 MultiPolygon 转换为 List<PolygonCoordinates>
        List<PolygonCoordinates> multiPolygonCoordinates = coordinateObjects.stream()
                .filter(obj -> obj instanceof List) // 过滤非法数据
                .map(polygonObj -> {
                    List<Object> polygonList = (List<Object>) polygonObj;
                    return getPolygonCoordinates(polygonList);
                })
                .collect(Collectors.toList());

        return new MultiPolygon(getCrs(document),multiPolygonCoordinates);
    }
}
