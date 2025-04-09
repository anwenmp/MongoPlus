package com.mongoplus.cache.global;

import com.mongodb.client.model.geojson.*;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.strategy.conversion.impl.*;
import com.mongoplus.strategy.conversion.impl.bson.*;
import com.mongoplus.strategy.conversion.impl.geo.*;
import com.mongoplus.toolkit.ClassTypeUtil;
import org.bson.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 转换器
 * @author anwen
 */
public class ConversionCache {

    private static final Map<Class<?>, ConversionStrategy<?>> conversionStrategieMap = new HashMap<>();

    /**
     * 可赋值类型的策略映射，用于匹配 X 类及其子类等具有继承关系的类型
     */
    private static final Map<Class<?>, ConversionStrategy<?>> assignableConversionMap = new HashMap<>();

    public static EnumConversionStrategy<?> enumConversion = new EnumConversionStrategy<>();

    public static Map<Class<?>, ConversionStrategy<? extends Geometry>> geometryConversionMap = new HashMap<>();

    static {
        conversionStrategieMap.put(Integer.class,new IntegerConversionStrategy());
        conversionStrategieMap.put(Long.class, new LongConversionStrategy());
        conversionStrategieMap.put(Double.class, new DoubleConversionStrategy());
        conversionStrategieMap.put(Float.class, new FloatConversionStrategy());
        conversionStrategieMap.put(Boolean.class, new BooleanConversionStrategy());
        conversionStrategieMap.put(String.class, new StringConversionStrategy());
        conversionStrategieMap.put(LocalTime.class,new LocalTimeConversionStrategy());
        conversionStrategieMap.put(LocalDate.class,new LocalDateConversionStrategy());
        conversionStrategieMap.put(LocalDateTime.class,new LocalDateTimeConversionStrategy());
        conversionStrategieMap.put(Date.class,new DateConversionStrategy());
        conversionStrategieMap.put(Instant.class,new InstantConversionStrategy());
        conversionStrategieMap.put(Object.class,new DefaultConversionStrategy());
        conversionStrategieMap.put(BigDecimal.class,new BigDecimalConversionStrategy());
        conversionStrategieMap.put(BigInteger.class,new BigIntegerConversionStrategy());
        conversionStrategieMap.put(Enum.class,enumConversion);
        conversionStrategieMap.put(Document.class,new DocumentConversionStrategy());
        conversionStrategieMap.put(byte[].class,new ByteArrayConversionStrategy());
        conversionStrategieMap.put(Byte.class,new ByteConversionStrategy());
        // bson
        conversionStrategieMap.put(BsonBoolean.class,new BsonBooleanConversionStrategy());
        conversionStrategieMap.put(BsonDateTime.class,new BsonDateTimeConversionStrategy());
        conversionStrategieMap.put(BsonDouble.class,new BsonDoubleConversionStrategy());
        conversionStrategieMap.put(BsonInt32.class,new BsonInt32ConversionStrategy());
        conversionStrategieMap.put(BsonInt64.class,new BsonInt64ConversionStrategy());
        conversionStrategieMap.put(BsonString.class,new BsonStringConversionStrategy());
        // geo
        geometryConversionMap.put(GeometryCollection.class,new GeometryCollectionConversionStrategy());
        geometryConversionMap.put(LineString.class,new LineStringConversionStrategy());
        geometryConversionMap.put(MultiLineString.class,new MultiLineStringConversionStrategy());
        geometryConversionMap.put(MultiPoint.class,new MultiPointConversionStrategy());
        geometryConversionMap.put(MultiPolygon.class,new MultiPolygonConversionStrategy());
        geometryConversionMap.put(Point.class,new PointConversionStrategy());
        geometryConversionMap.put(Polygon.class,new PolygonConversionStrategy());
        conversionStrategieMap.putAll(geometryConversionMap);
        conversionStrategieMap.put(Geometry.class,new GeometryConversionStrategy());
    }

    public static ConversionStrategy<?> getConversionStrategy(Class<?> clazz){
        ConversionStrategy<?> conversionStrategy = conversionStrategieMap.get(clazz);

        if (conversionStrategy == null) {
            conversionStrategy = assignableConversionMap.entrySet().stream()
                    .filter(entry -> ClassTypeUtil.isTargetClass(entry.getKey(),clazz))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return conversionStrategy;
    }

    public static void putConversionStrategy(Class<?> clazz,ConversionStrategy<?> conversionStrategy){
        if (clazz.equals(Enum.class)) {
            enumConversion = (EnumConversionStrategy<?>) conversionStrategy;
        }
        conversionStrategieMap.put(clazz,conversionStrategy);
    }

}
