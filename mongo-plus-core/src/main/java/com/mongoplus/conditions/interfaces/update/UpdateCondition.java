package com.mongoplus.conditions.interfaces.update;

import com.mongoplus.conditions.interfaces.update.array.Array;
import com.mongoplus.conditions.interfaces.update.field.Field;
import org.bson.conversions.Bson;

/**
 * 修改条件
 *
 * @author anwen
 */
public interface UpdateCondition<T, Children> extends
        Array<T, Children>,
        Field<T, Children> {

    /**
     * 自定义修改
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     */
    Children updateCustom(Bson bson);


}
