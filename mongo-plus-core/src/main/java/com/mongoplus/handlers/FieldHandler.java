package com.mongoplus.handlers;

import com.mongoplus.mapping.FieldInformation;

import java.util.function.Function;

public interface FieldHandler {

    /**
     * 该处理器的顺序，从小到大
     * @return 顺序值
     */
    default Integer order() {
        return 0;
    }

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
    @Deprecated
    Object handler(FieldInformation fieldInformation);

    /**
     * 处理字段，并接收前序处理器产生的最新值。
     * 默认委托旧的单参数方法，以兼容现有实现。
     *
     * @param fieldInformation 字段信息
     * @param currentValue 当前字段值
     * @return 新的字段值，返回 null 表示不替换当前值
     */
    default Object handler(FieldInformation fieldInformation, Object currentValue) {
        return handler(fieldInformation);
    }

}
