package com.mongoplus.function;

import com.mongoplus.support.SFunction;
import com.mongoplus.toolkit.FunctionUtil;

/**
 * 嵌套字段
 *
 * @author anwen
 */
public class FieldChain<T> {

    private final FunctionUtil.FunctionBuilder builder;

    private FieldChain(SFunction<?,?> root) {
        builder = FunctionUtil.builderFunction();
        builder.add(root);
    }

    public static <T> FieldChain<T> of(SFunction<T,?> root) {
        return new FieldChain<>(root);
    }

    @SuppressWarnings("unchecked")
    public <R> FieldChain<R> then(SFunction<R,?> function) {
        builder.add(function);
        return (FieldChain<R>) this;
    }

    public String build() {
        return builder.build();
    }

    public String build(boolean isOption) {
        return builder.build(isOption);
    }

    public String buildWithDollar() {
        return build(true);
    }

}
