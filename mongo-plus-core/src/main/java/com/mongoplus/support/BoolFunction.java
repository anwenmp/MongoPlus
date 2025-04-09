package com.mongoplus.support;


import java.lang.reflect.Method;

/**
 * boolean function
 *
 * @author loser
 */
@FunctionalInterface
public interface BoolFunction {

    boolean get(Object proxy, Object target, Method method, Object[] args);

}
