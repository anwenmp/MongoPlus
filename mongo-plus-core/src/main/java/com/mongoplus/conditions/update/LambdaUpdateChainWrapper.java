package com.mongoplus.conditions.update;

import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongoplus.mapper.BaseMapper;

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
    public boolean updateOne(UpdateOptions options) {
        return baseMapper.updateOne(this,clazz,options);
    }

    @Override
    public boolean remove(DeleteOptions options) {
        return baseMapper.remove(this,clazz,options);
    }

    @Override
    public boolean removeOne(DeleteOptions options) {
        return baseMapper.removeOne(this,clazz,options);
    }

}
