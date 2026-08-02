package com.mongoplus.handler;

import com.mongoplus.annotation.SensitiveWord;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.handlers.FieldHandler;
import com.mongoplus.manager.SensitiveWordManager;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.registry.HandlerRegistry;

/**
 * 关键字字段处理器
 */
public class SensitiveWordFieldHandler implements FieldHandler {

    private final SensitiveWordManager sensitiveWordManager;

    public SensitiveWordFieldHandler() {
        sensitiveWordManager = HandlerRegistry.get(SensitiveWordManager.class);
        if (sensitiveWordManager == null) {
            throw new MongoPlusException("SensitiveWordManager is null");
        }
    }

    @Override
    public Integer order() {
        return Integer.MIN_VALUE;
    }

    @Override
    public Object handler(FieldInformation fieldInformation) {
        return handler(fieldInformation, fieldInformation.getValue());
    }

    @Override
    public Object handler(FieldInformation fieldInformation, Object currentValue) {
        SensitiveWord sensitiveWord = fieldInformation.getAnnotation(SensitiveWord.class);
        if (sensitiveWord != null && currentValue != null) {
            sensitiveWordManager.handler(fieldInformation.getName(), String.valueOf(currentValue));
        }
        return null;
    }
}
