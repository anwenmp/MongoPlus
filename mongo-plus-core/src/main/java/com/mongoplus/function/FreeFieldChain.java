package com.mongoplus.function;

import com.mongoplus.support.SFunction;
import com.mongoplus.toolkit.FunctionUtil;

/**
 * 自由的构建嵌套字段,和{@link FieldChain}类不同,不拘泥于lambda
 * @author anwen
 */
public class FreeFieldChain {

    private final FunctionUtil.FunctionBuilder builder;

    private FreeFieldChain() {
        builder = FunctionUtil.builderFunction();
    }

    public static FreeFieldChain create() {
        return new FreeFieldChain();
    }

    public <T> FreeFieldChain append(SFunction<T,?> function) {
        builder.add(function);
        return this;
    }

    public FreeFieldChain append(String fieldName) {
        builder.add(fieldName);
        return this;
    }

    public FreeFieldChain append(Class<?> from) {
        builder.add(from);
        return this;
    }

    public String build() {
        return builder.build();
    }

    public String build(boolean isOption) {
        return builder.build(isOption);
    }

    public String buildOption() {
        return build(true);
    }

}
