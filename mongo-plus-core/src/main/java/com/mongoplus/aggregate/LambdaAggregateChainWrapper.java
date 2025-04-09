package com.mongoplus.aggregate;

import com.mongoplus.mapper.BaseMapper;

import java.util.List;

/**
 * @author anwen
 */
public class LambdaAggregateChainWrapper<T> extends LambdaAggregateWrapper<LambdaAggregateChainWrapper<T>> implements ChainAggregate<T> {

    private final BaseMapper baseMapper;

    private final Class<T> clazz;

    public LambdaAggregateChainWrapper(BaseMapper baseMapper, Class<T> clazz) {
        this.baseMapper = baseMapper;
        this.clazz = clazz;
    }

    @Override
    public List<T> list() {
        return list(clazz);
    }

    @Override
    public <R> List<R> list(Class<R> rClazz) {
        return baseMapper.aggregateList(this,clazz,rClazz);
    }

    @Override
    public T one() {
        return one(clazz);
    }

    @Override
    public <R> R one(Class<R> rClazz) {
        return baseMapper.aggregateOne(this,clazz,rClazz);
    }
}
