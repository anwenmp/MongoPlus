package com.anwen.mongo.support;

import com.anwen.mongo.interceptor.Invocation;

@FunctionalInterface
public interface AdvancedFunction {

    boolean get(Invocation invocation);

}
