package com.anwen.mongo.toolkit;

import com.anwen.mongo.aggregate.LambdaAggregateChainWrapper;
import com.anwen.mongo.conditions.query.LambdaQueryChainWrapper;
import com.anwen.mongo.conditions.query.QueryChainWrapper;
import com.anwen.mongo.conditions.query.QueryWrapper;
import com.anwen.mongo.conditions.update.LambdaUpdateChainWrapper;
import com.anwen.mongo.conditions.update.UpdateChainWrapper;
import com.anwen.mongo.conditions.update.UpdateWrapper;
import com.anwen.mongo.mapper.BaseMapper;

/**
 * 快速构建链式调用
 * @author JiaChaoYang
 * @date 2023/6/24/024 2:27
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
