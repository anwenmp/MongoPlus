package com.mongoplus.conditions.interfaces.update.field;

import com.mongoplus.conditions.interfaces.update.field.operations.*;

/**
 * 字段更新操作
 *
 * @author anwen
 */
public interface Field<T, Children> extends
        CurrentDate<T, Children>,
        Inc<T, Children>,
        Max<T, Children>,
        Min<T, Children>,
        Mul<T, Children>,
        Rename<T, Children>,
        Set<T, Children>,
        SetOnInsert<T, Children>,
        Unset<T, Children> {
}
