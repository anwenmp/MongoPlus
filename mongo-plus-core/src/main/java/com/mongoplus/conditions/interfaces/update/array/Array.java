package com.mongoplus.conditions.interfaces.update.array;

import com.mongoplus.conditions.interfaces.update.array.operations.*;

/**
 * 数组操作
 *
 * @author anwen
 */
public interface Array<T, Children> extends
        AddToSet<T, Children>,
        Pop<T, Children>,
        Pull<T, Children>,
        PullAll<T, Children>,
        Push<T, Children> {
}
