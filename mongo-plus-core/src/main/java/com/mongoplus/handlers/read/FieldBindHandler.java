package com.mongoplus.handlers.read;

import com.mongoplus.annotation.collection.FieldBind;
import com.mongoplus.domain.MongoPlusFieldException;
import com.mongoplus.handlers.DataBindHandler;
import com.mongoplus.handlers.ReadHandler;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.toolkit.StringUtils;

import java.util.function.Function;

/**
 * 字段数据绑定写入处理器
 *
 * @author anwen
 */
public class FieldBindHandler implements ReadHandler {

    /**
     * 数据绑定处理器
     */
    private final DataBindHandler dataBindHandler;

    public FieldBindHandler(DataBindHandler dataBindHandler) {
        this.dataBindHandler = dataBindHandler;
    }

    @Override
    public Function<FieldInformation, Boolean> activate() {
        return fieldInformation -> fieldInformation.isAnnotation(FieldBind.class);
    }

    @Override
    public Object read(FieldInformation fieldInformation, Object source) {
        FieldBind fieldBind = fieldInformation.getAnnotation(FieldBind.class);
        String target = StringUtils.camelToUnderline(fieldBind.target());
        TypeInformation typeInformation = TypeInformation.of(fieldInformation.getInstance());
        FieldInformation targetFieldInformation = typeInformation.getField(target);
        if (targetFieldInformation == null) {
            throw new MongoPlusFieldException("fieldBind target field not found");
        }
        dataBindHandler.setMetaObject(fieldInformation.getAnnotation(FieldBind.class), source, targetFieldInformation);
        return source;
    }
}
