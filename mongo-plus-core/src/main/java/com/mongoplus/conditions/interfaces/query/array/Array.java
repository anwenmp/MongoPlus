package com.mongoplus.conditions.interfaces.query.array;

import com.mongoplus.conditions.interfaces.query.array.operations.All;
import com.mongoplus.conditions.interfaces.query.array.operations.ElemMatch;
import com.mongoplus.conditions.interfaces.query.array.operations.Size;

/**
 * 数组操作
 *
 * @author anwen
 */
public interface Array<T, Children> extends
        All<T, Children>,
        ElemMatch<T, Children>,
        Size<T, Children> {
}
