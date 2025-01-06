package com.mongoplus.handlers.auto;

import com.mongoplus.enums.FieldFill;
import com.mongoplus.handlers.MetaObjectHandler;
import com.mongoplus.model.AutoFillMetaObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class DefaultAutoFillHandler extends AbstractAutoFillHandler {

    Map<FieldFill, BiConsumer<AutoFillMetaObject, MetaObjectHandler>> fillConsumerMap = new HashMap<>();

    public DefaultAutoFillHandler() {
        fillConsumerMap.put(FieldFill.INSERT,
                (autoFillMetaObject, metaObjectHandler) ->
                        metaObjectHandler.insertFill(autoFillMetaObject));
        fillConsumerMap.put(FieldFill.UPDATE,
                (autoFillMetaObject, metaObjectHandler) ->
                        metaObjectHandler.updateFill(autoFillMetaObject));
    }

    @Override
    protected void fillHandle(AutoFill autoFill, AutoFillMetaObject autoFillMetaObject) {
        fillConsumerMap.get(autoFill.getFieldFill()).accept(autoFillMetaObject, autoFill.getMetaObjectHandler());
    }
}
