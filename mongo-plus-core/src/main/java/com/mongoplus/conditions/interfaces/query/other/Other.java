package com.mongoplus.conditions.interfaces.query.other;

import com.mongoplus.conditions.interfaces.query.other.operations.*;

/**
 * 其他查询操作
 *
 * @author anwen
 */
public interface Other<T, Children> extends
        Expr<T, Children>,
        JsonSchema<T, Children>,
        Like<T, Children>,
        Mod<T, Children>,
        Regex<T, Children>,
        Where<T, Children> {

}
