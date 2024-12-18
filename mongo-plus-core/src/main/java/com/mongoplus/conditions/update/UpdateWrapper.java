package com.mongoplus.conditions.update;

import com.mongoplus.toolkit.ChainWrappers;

/**
 * @author JiaChaoYang
 **/
public class UpdateWrapper<T> extends UpdateChainWrapper<T,UpdateWrapper<T>> {

    /**
     * 链式调用
     * @author JiaChaoYang
     * @date 2023/8/12 2:14
     */
    public UpdateChainWrapper<T, UpdateWrapper<T>> lambdaUpdate(){
        return ChainWrappers.lambdaUpdateChain();
    }
}
