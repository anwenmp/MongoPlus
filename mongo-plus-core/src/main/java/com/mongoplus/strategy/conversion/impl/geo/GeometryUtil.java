package com.mongoplus.strategy.conversion.impl.geo;

import com.mongodb.client.model.geojson.*;
import org.bson.Document;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author anwen
 */
@SuppressWarnings("unchecked")
public class GeometryUtil {

    static List<Position> getPosition(Object fieldValue) {
        List<Position> positions = new LinkedList<>();
        Document document = (Document) fieldValue;
        List<Object> coordinates = document.getList("coordinates", Object.class);
        coordinates.forEach(coordinate -> positions.add(new Position((List<Double>) coordinate)));
        return positions;
    }


    static PolygonCoordinates getPolygonCoordinates(List<Object> coordinates) {
        // 构建外环（polygonList 的第一个元素）
        List<Position> exterior = Optional.ofNullable(coordinates.get(0))
                .filter(ring -> ring instanceof List)
                .map(ring -> ((List<?>) ring).stream()
                        .filter(point -> point instanceof List)
                        .map(point -> new Position((List<Double>) point))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        // 构建内环（polygonList 的第 1 到 n 个元素）
        /*
          NOTE:
          数据库中的数据,数据结构并不是PolygonCoordinates类中的holes属性那样的'二维数组'结构,
            而是将二维数组转成了一维数组,和exterior属性同级.
          所以转换需要从一维数组还原成二维数组,即holes属性的二维数组结构
          可以参考下com.mongodb.client.model.geojson.PolygonCoordinates类中的两个属性
          */
        List<List<Position>> interiors = coordinates.stream()
                .skip(1)
                .filter(ring -> ring instanceof List)
                .map(ring -> ((List<?>) ring).stream()
                        .filter(point -> point instanceof List)
                        .map(point -> new Position((List<Double>) point))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        return new PolygonCoordinates(exterior, interiors);
    }

    static CoordinateReferenceSystem getCrs(Document document) {
        CoordinateReferenceSystem coordinateReferenceSystem = null;
        Document crsDocument = document.get("crs", Document.class);
        if (crsDocument != null) {
            String type = crsDocument.getString("type");
            if (Objects.equals(type, CoordinateReferenceSystemType.NAME.getTypeName())) {
                String name = crsDocument.get("properties",Document.class).getString("name");
                coordinateReferenceSystem = new NamedCoordinateReferenceSystem(name);
            }
        }
        return coordinateReferenceSystem;
    }

}
