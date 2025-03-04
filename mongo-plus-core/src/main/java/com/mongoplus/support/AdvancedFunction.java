package com.mongoplus.support;

import com.mongoplus.interceptor.Invocation;

@FunctionalInterface
public interface AdvancedFunction {

    boolean get(Invocation invocation);

}
