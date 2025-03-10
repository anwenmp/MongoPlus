package com.mongoplus.cache.global;

import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.strategy.conversion.impl.*;
import com.mongoplus.strategy.conversion.impl.bson.*;
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
 * @date 2024/5/8 下午9:12
 */
public class ConversionCache {

    private static final Map<Class<?>, ConversionStrategy<?>> conversionStrategieMap = new HashMap<>();

    public static EnumConversionStrategy<?> enumConversion = new EnumConversionStrategy<>();

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
    }

    public static ConversionStrategy<?> getConversionStrategy(Class<?> clazz){
        return conversionStrategieMap.get(clazz);
    }

    public static void putConversionStrategy(Class<?> clazz,ConversionStrategy<?> conversionStrategy){
        if (clazz.equals(Enum.class)) {
            enumConversion = (EnumConversionStrategy<?>) conversionStrategy;
        }
        conversionStrategieMap.put(clazz,conversionStrategy);
    }

}
