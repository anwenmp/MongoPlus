package com.mongoplus.handlers;

import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.MongoConverter;

import java.util.function.Function;

/**
 * 映射处理器
 *
 * @author anwen
 */
public interface ReadHandler {

    /**
     * 该处理器的顺序，从小到大
     * @author anwen
     */
    default Integer order(){
        return Integer.MAX_VALUE;
    }

    /**
     * 获取一些额外的值，
     * @return {@link java.lang.Object}
     * @author anwen
     */
    default Object getExtraValue(Object object) {
        return object;
    }

    /**
     * 是否处于激活状态
     * @return {@link java.util.function.Function}
     * @author anwen
     */
    default Function<FieldInformation,Boolean> activate() {
        return fieldInformation -> true;
    }

    /**
     * 终止读取
     * @return {@link boolean}
     * @author anwen
     */
    default Function<Object,Boolean> discontinue() {
        return object -> false;
    }

    /**
     * 映射的处理方法，在映射处理后，写入属性值前
     * @param fieldInformation Field的一些信息
     * @param source 要写入field的值
     * @return {@link java.lang.Object}
     * @author anwen
     */
    default Object read(FieldInformation fieldInformation,Object source) {
        return source;
    }

    /**
     * 映射的处理方法，在映射处理后，写入属性值前
     * @param fieldInformation Field的一些信息
     * @param source 要写入field的值
     * @param mongoConverter mongoConvert
     * @return {@link java.lang.Object}
     * @author anwen
     */
    default Object read(FieldInformation fieldInformation, Object source, MongoConverter mongoConverter) {
        return read(fieldInformation,source);
    }

}
