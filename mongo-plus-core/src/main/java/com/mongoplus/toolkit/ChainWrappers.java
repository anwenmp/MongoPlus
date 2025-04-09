package com.mongoplus.toolkit;

import com.mongoplus.aggregate.LambdaAggregateChainWrapper;
import com.mongoplus.conditions.query.LambdaQueryChainWrapper;
import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.conditions.update.LambdaUpdateChainWrapper;
import com.mongoplus.conditions.update.UpdateChainWrapper;
import com.mongoplus.conditions.update.UpdateWrapper;
import com.mongoplus.mapper.BaseMapper;

/**
 * 快速构建链式调用
 * @author JiaChaoYang
*/
public final class ChainWrappers {

    public static <T> LambdaQueryChainWrapper<T> lambdaQueryChain(BaseMapper baseMapper, Class<T> clazz){
        return new LambdaQueryChainWrapper<>(baseMapper, clazz);
    }

    public static <T> LambdaAggregateChainWrapper<T> lambdaAggregatesChain(BaseMapper baseMapper, Class<T> clazz){
        return new LambdaAggregateChainWrapper<>(baseMapper, clazz);
    }

    public static <T> LambdaUpdateChainWrapper<T> lambdaUpdateChain(BaseMapper baseMapper, Class<T> clazz){
        return new LambdaUpdateChainWrapper<>(baseMapper, clazz);
    }

    public static <T> UpdateChainWrapper<T, UpdateWrapper<T>> lambdaUpdateChain(){
        return new UpdateChainWrapper<>();
    }

    public static <T> QueryChainWrapper<T, QueryWrapper<T>> lambdaQueryChain(){
        return new QueryWrapper<>();
    }

}
