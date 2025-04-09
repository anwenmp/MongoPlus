package com.mongoplus.toolkit;

import com.mongoplus.aggregate.AggregateWrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.conditions.update.UpdateWrapper;

/**
 * 快捷获取条件构造器
 *
 * @author anwen
 */
public class Wrappers {

    /**
     * 获取条件构造器
     * @author anwen
     */
    public static <T> QueryWrapper<T> lambdaQuery(){
        return new QueryWrapper<>();
    }

    /**
     * 获取修改条件构造器
     * @author anwen
     */
    public static <T> UpdateWrapper<T> lambdaUpdate(){
        return new UpdateWrapper<>();
    }

    /**
     * 获取聚合条件构造器
     * @author anwen
     */
    public static AggregateWrapper lambdaAggregate(){
        return new AggregateWrapper();
    }

}
