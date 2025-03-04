package com.mongoplus.conditions.update;

/**
 * Null的宿主类
 *
 * @author anwen
 */
public final class Holder {

    private Holder() {
    }

    public static Object isNull(Object source) {
        return source == Null.class ? null : source;
    }

}
