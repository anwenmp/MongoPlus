package com.mongoplus.conditions.interfaces.query.bitwise;

import com.mongoplus.conditions.interfaces.query.bitwise.operations.BitsAllClear;
import com.mongoplus.conditions.interfaces.query.bitwise.operations.BitsAllSet;
import com.mongoplus.conditions.interfaces.query.bitwise.operations.BitsAnyClear;
import com.mongoplus.conditions.interfaces.query.bitwise.operations.BitsAnySet;

/**
 * 按位操作
 *
 * @author anwen
 */
public interface Bitwise<T, Children> extends
        BitsAllSet<T, Children>,
        BitsAnySet<T, Children>,
        BitsAllClear<T, Children>,
        BitsAnyClear<T, Children> {
}
