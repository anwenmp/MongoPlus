package com.mongoplus.conditions.interfaces.query.data;

import com.mongoplus.conditions.interfaces.query.data.operations.Exists;
import com.mongoplus.conditions.interfaces.query.data.operations.Type;

/**
 * 数据类型查询
 *
 * @author anwen
 */
public interface DataType<T, Children> extends
        Exists<T, Children>,
        Type<T, Children> {
}
