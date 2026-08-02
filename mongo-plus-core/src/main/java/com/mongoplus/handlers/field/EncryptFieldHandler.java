package com.mongoplus.handlers.field;

import com.mongoplus.annotation.comm.FieldEncrypt;
import com.mongoplus.handlers.FieldHandler;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.toolkit.EncryptorUtil;

import java.util.function.Function;

/**
 * 加密的字段处理
 * @author anwen
 */
public class EncryptFieldHandler implements FieldHandler {

    @Override
    public Integer order() {
        return Integer.MAX_VALUE - 1;
    }

    @Override
    public Function<FieldInformation, Boolean> activate() {
        return (fieldInformation -> fieldInformation.isAnnotation(FieldEncrypt.class));
    }

    @Override
    public Object handler(FieldInformation fieldInformation) {
        return handler(fieldInformation, fieldInformation.getValue());
    }

    @Override
    public Object handler(FieldInformation fieldInformation, Object currentValue) {
        return EncryptorUtil.encrypt(fieldInformation.getAnnotation(FieldEncrypt.class), currentValue);
    }
}
