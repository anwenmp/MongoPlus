package com.anwen.mongo.toolkit;

import com.anwen.mongo.support.SFunction;

import java.util.LinkedList;
import java.util.List;

/**
 * Function工具类
 *
 * @author anwen
 */
public class FunctionUtil {

    /**
     * lambda形式获取字段名
     *
     * @author anwen
     */
    public static <T> String getFieldName(SFunction<T, ?> function) {
        return function.getFieldNameLine();
    }

    /**
     * lambda形式获取字段名，带$符
     *
     * @author anwen
     */
    public static <T> String getFieldNameOption(SFunction<T, ?> function) {
        return function.getFieldNameLineOption();
    }

    /**
     * 构建FunctionBuild，用于拼接文档内部属性
     * <p>例：</p>{@code FunctionUtil.builderFunction().add(User::getRole).add(Role::getAge).build()} -> role.age
     * @author anwen
     */
    public static FunctionBuilder builderFunction() {
        return new FunctionBuilder();
    }

    /**
     * 构建FunctionBuild，用于拼接文档内部属性
     * <p>例：</p>{@code FunctionUtil.builderFunction().add(User::getRole).add(Role::getAge).build()} -> role.age
     * @author anwen
     */
    public static FunctionBuilder builderFunction(List<SFunction<?,?>> functionList) {
        return new FunctionBuilder(functionList);
    }

    public static class FunctionBuilder {

        private final List<SFunction<?, ?>> functionList;

        public FunctionBuilder(List<SFunction<?, ?>> functionList) {
            this.functionList = functionList;
        }

        public FunctionBuilder() {
            this.functionList = new LinkedList<>();
        }

        public <T> FunctionBuilder add(SFunction<T, ?> function) {
            this.functionList.add(function);
            return this;
        }

        /**
         * 构建字段
         * @param isOption 是否拼接$符
         * @return {@link String}
         * @author anwen
         */
        public String build(boolean isOption) {
            StringBuilder fieldNameBuffer = new StringBuilder();
            functionList.forEach(function -> fieldNameBuffer.append(".").append(getFieldName(function)));
            if (isOption){
                fieldNameBuffer.setCharAt(0,'$');
            } else {
                fieldNameBuffer.deleteCharAt(0);
            }
            return fieldNameBuffer.toString();
        }

        /**
         * 构建字段，不拼接$符
         * @return {@link String}
         * @author anwen
         */
        public String build() {
            return build(false);
        }

    }

}
