package com.mongoplus.annotation.collection;

import com.mongodb.client.model.TimeSeriesGranularity;

import java.lang.annotation.*;

/**
 * 指定为时间序列集合
 * @author anwen
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TimeSeries {

    /**
     * 数据源，指定时间序列所在的数据源
     */
    String dataSource() default "";

    /**
     * 包含每个时间序列文档中的日期的字段的名称
     * <p>可使用{@code $}标识，该操作将会字段值查找类中的该字段，不存在则直接返回该值，如：</p>
     * <p>
     *      {@code @TimeSeries("$field1"})}
     * </p>
     */
    String timeField();

    /**
     * 包含每个时间序列文档中的元数据的字段的名称
     * <p>可使用{@code $}标识，该操作将会字段值查找类中的该字段，不存在则直接返回该值，如：</p>
     * <p>
     *      {@code @TimeSeries(metaField="$field1"})}
     * </p>
     */
    String metaField() default "";

    /**
     * 指定时间序列的粒度
     */
    TimeSeriesGranularity granularity() default TimeSeriesGranularity.SECONDS;

    /**
     * 设置存储桶中测量之间的最大时间跨度，以秒为单位
     */
    long bucketMaxSpan() default -1;

    /**
     * 指定确定新存储桶的起始时间戳的时间间隔，以秒为单位
     */
    long bucketRounding() default -1;

    /**
     * 指定文档过期的秒数
     */
    long expireAfter() default -1;

}
