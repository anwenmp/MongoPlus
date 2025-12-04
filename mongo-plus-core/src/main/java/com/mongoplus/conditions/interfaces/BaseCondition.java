package com.mongoplus.conditions.interfaces;

/**
 * 基础接口
 *
 * @author anwen
 */
public interface BaseCondition<T, Children> {

    @SuppressWarnings("unchecked")
    default Children typeThis() {
        return (Children) this;
    }

}
