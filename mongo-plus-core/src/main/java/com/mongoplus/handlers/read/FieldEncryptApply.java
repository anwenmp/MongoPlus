package com.mongoplus.handlers.read;

import com.mongoplus.annotation.comm.FieldEncrypt;
import com.mongoplus.handlers.ReadHandler;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.toolkit.EncryptorUtil;

/**
 * 加密处理器
 *
 * @author anwen
 */
public class FieldEncryptApply implements ReadHandler {

    @Override
    public Integer order() {
        return 0;
    }

    @Override
    public Object read(FieldInformation fieldInformation, Object source) {
        FieldEncrypt fieldEncrypt = fieldInformation.getAnnotation(FieldEncrypt.class);
        if (fieldEncrypt != null && fieldEncrypt.findDecrypt()){
            source = EncryptorUtil.decrypt(fieldInformation.getAnnotation(FieldEncrypt.class),source);
        }
        return source;
    }
}
