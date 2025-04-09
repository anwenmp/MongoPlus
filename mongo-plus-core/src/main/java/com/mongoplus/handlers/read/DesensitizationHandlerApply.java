package com.mongoplus.handlers.read;

import com.mongoplus.annotation.comm.Desensitization;
import com.mongoplus.domain.MongoPlusConvertException;
import com.mongoplus.handlers.DesensitizationHandler;
import com.mongoplus.handlers.ReadHandler;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.toolkit.ClassTypeUtil;
import com.mongoplus.toolkit.DesensitizedUtil;

/**
 * 脱敏处理器
 *
 * @author anwen
 */
public class DesensitizationHandlerApply implements ReadHandler {

    @Override
    public Integer order() {
        return 1;
    }

    @Override
    public Object read(FieldInformation fieldInformation, Object source) {
        Desensitization desensitization = fieldInformation.getAnnotation(Desensitization.class);
        if (fieldInformation.isAnnotation(Desensitization.class)){
            Class<?> desensitizationClass = desensitization.desensitizationHandler();
            if (desensitizationClass != Void.class && ClassTypeUtil.isTargetClass(DesensitizationHandler.class,desensitizationClass)){
                DesensitizationHandler desensitizationHandler = (DesensitizationHandler) ClassTypeUtil.getInstanceByClass(desensitizationClass);
                source = desensitizationHandler.desensitized(fieldInformation.getField(),
                        source, desensitization.startInclude(), desensitization.endExclude(), desensitization.type());
            } else {
                String desensitizationValue;
                try {
                    desensitizationValue = String.valueOf(source);
                } catch (Exception e) {
                    throw new MongoPlusConvertException("Fields that require desensitization cannot be converted to strings");
                }
                source = DesensitizedUtil.desensitized(desensitizationValue, desensitization.startInclude(), desensitization.endExclude(), desensitization.type());
            }
        }
        return source;
    }
}
