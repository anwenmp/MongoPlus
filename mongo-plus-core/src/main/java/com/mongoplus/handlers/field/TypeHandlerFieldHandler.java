package com.mongoplus.handlers.field;

import com.mongoplus.handlers.FieldHandler;
import com.mongoplus.handlers.TypeHandler;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.toolkit.ClassTypeUtil;

import java.util.function.Function;

/**
 * TypeHandler的处理
 *
 * @author anwen
 */
public class TypeHandlerFieldHandler implements FieldHandler {

    @Override
    public Function<FieldInformation, Boolean> activate() {
        return (fieldInformation -> fieldInformation.getCollectionField() != null &&
                ClassTypeUtil.isTargetClass(TypeHandler.class, fieldInformation.getCollectionField().typeHandler()));
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object handler(FieldInformation fieldInformation) {
        TypeHandler typeHandler = (TypeHandler) ClassTypeUtil.getInstanceByClass(fieldInformation.getCollectionField()
                .typeHandler());
        return typeHandler.setParameter(fieldInformation.getName(), fieldInformation.getValue());
    }
}
