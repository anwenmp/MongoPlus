package com.mongoplus.handlers;

import com.mongoplus.mapping.FieldInformation;

import java.util.function.Function;

public interface FieldHandler {

    /**
     * 是否处于激活状态
     * @return {@link java.util.function.Function}
     * @author anwen
     */
    default Function<FieldInformation,Boolean> activate() {
        return (fieldInformation) -> true;
    }

    /**
     * 处理字段
     * @param fieldInformation 字段信息
     * @return {@link java.lang.Object}
     * @author anwen
     */
    Object handler(FieldInformation fieldInformation);

}
