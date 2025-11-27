package com.mongoplus.conditions.interfaces.compare;

import com.mongoplus.conditions.interfaces.compare.operations.*;

/**
 * 比较条件
 * @author anwen
 */
public interface Compare<T, Children> extends
        Eq<T, Children>,
        Gt<T, Children>,
        Gte<T, Children>,
        In<T, Children>,
        Lt<T, Children>,
        Lte<T, Children>,
        Ne<T, Children>,
        Nin<T, Children> {


}
