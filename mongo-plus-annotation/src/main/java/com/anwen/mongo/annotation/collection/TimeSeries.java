package com.anwen.mongo.annotation.collection;

import com.mongodb.client.model.TimeSeriesGranularity;

/**
 * 指定为时间序列集合
 * @author anwen
 */
public @interface TimeSeries {

    /**
     * 数据源，指定时间序列所在的数据源
     * @date 2024/8/27 01:35
     */
    String dataSource() default "";

    /**
     * 包含每个时间序列文档中的日期的字段的名称
     * @date 2024/8/27 01:23
     */
    String timeField();

    /**
     * 包含每个时间序列文档中的元数据的字段的名称
     * @date 2024/8/27 01:23
     */
    String metaField() default "";

    /**
     * 指定时间序列的粒度
     * @date 2024/8/27 01:23
     */
    TimeSeriesGranularity granularity() default TimeSeriesGranularity.SECONDS;

    /**
     * 设置存储桶中测量之间的最大时间跨度，以秒为单位
     * @date 2024/8/27 01:30
     */
    long bucketMaxSpan() default -1;

    /**
     * 指定确定新存储桶的起始时间戳的时间间隔，以秒为单位
     * @date 2024/8/27 01:31
     */
    long bucketRounding() default -1;

    /**
     * 自动创建时间序列集合
     * <p>开启此配置，在项目启动时会进行查询，然后创建</p>
     * @date 2024/8/27 01:32
     */
    boolean autoCreateTimeSeries() default true;

}
