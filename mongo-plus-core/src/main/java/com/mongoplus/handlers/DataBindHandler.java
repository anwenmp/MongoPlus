package com.mongoplus.handlers;

import com.mongoplus.annotation.collection.FieldBind;
import com.mongoplus.mapping.FieldInformation;

/**
 * 字段数据绑定处理器
 *
 * @author anwen
 */
public interface DataBindHandler {

    /**
     * 设置元数据对象<br>
     * 根据源对象映射绑定指定属性（自行处理缓存逻辑）
     *
     * @param fieldBind  数据绑定注解
     * @param fieldValue 属性值
     * @param fieldInformation 元数据对象
     */
    void setMetaObject(FieldBind fieldBind, Object fieldValue, FieldInformation fieldInformation);

}
