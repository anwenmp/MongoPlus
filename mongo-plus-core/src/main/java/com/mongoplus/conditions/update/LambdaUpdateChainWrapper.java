package com.mongoplus.conditions.update;

import com.mongoplus.mapper.BaseMapper;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.UpdateOptions;

public class LambdaUpdateChainWrapper<T> extends UpdateChainWrapper<T,LambdaUpdateChainWrapper<T>> implements ChainUpdate {

    private final BaseMapper baseMapper;

    private final Class<T> clazz;

    public LambdaUpdateChainWrapper(BaseMapper baseMapper, Class<T> clazz) {
        this.baseMapper = baseMapper;
        this.clazz = clazz;
    }

    @Override
    public boolean update(UpdateOptions options){
        return baseMapper.update(this,clazz,options);
    }

    @Override
    public boolean remove(DeleteOptions options) {
        return baseMapper.remove(this,clazz,options);
    }

}
