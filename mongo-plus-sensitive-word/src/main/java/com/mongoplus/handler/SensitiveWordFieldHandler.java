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
    public Object handler(FieldInformation fieldInformation) {
        SensitiveWord sensitiveWord = fieldInformation.getAnnotation(SensitiveWord.class);
        Object value = fieldInformation.getValue();
        if (sensitiveWord != null && value != null) {
            sensitiveWordManager.handler(fieldInformation);
        }
        return value;
    }
}
