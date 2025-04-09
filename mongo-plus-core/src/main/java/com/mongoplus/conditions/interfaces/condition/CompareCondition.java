package com.mongoplus.conditions.interfaces.condition;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * 构建条件对象
 * @author JiaChaoYang
 * @since 2023/2/14 14:13
*/
public class CompareCondition implements Serializable {

    private static final long serialVersionUID = 682242054799754195L;

    /**
     * 条件
     */
    private String condition;

    /**
     * 字段
    */
    private String column;

    /**
     * 值
     * @since 2023/2/10 10:16
    */
    private Object value;

    /**
     * 原始class
     */
    private Class<?> originalClass;

    /**
     * 原始Field
     */
    private Field originalField;

    /**
     * 一些额外的值
     */
    private Object extraValue;

    public static CompareConditionBuilder builder() {
        return new CompareConditionBuilder();
    }

    public String getCondition() {
        return this.condition;
    }

    public String getColumn() {
        return this.column;
    }

    public Object getValue() {
        return this.value;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(Class<T> clazz){
        return (T) this.value;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Class<?> getOriginalClass() {
        return originalClass;
    }

    public void setOriginalClass(Class<?> originalClass) {
        this.originalClass = originalClass;
    }

    public Field getOriginalField() {
        return originalField;
    }

    public void setOriginalField(Field originalField) {
        this.originalField = originalField;
    }

    public Object getExtraValue() {
        return extraValue;
    }

    @SuppressWarnings("unchecked")
    public <T> T getExtraValue(Class<T> clazz) {
        return (T) extraValue;
    }

    public void setExtraValue(Object extraValue) {
        this.extraValue = extraValue;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        CompareCondition that = (CompareCondition) object;
        return Objects.equals(condition, that.condition) && Objects.equals(column, that.column) && Objects.equals(value, that.value) && Objects.equals(originalClass, that.originalClass) && Objects.equals(originalField, that.originalField);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, column, value, originalClass, originalField);
    }

    @Override
    public String toString() {
        return "{" +
                "condition='" + condition + '\'' +
                "column='" + column + '\'' +
                "value=" + value +
                "originalClass=" + originalClass +
                "originalField=" + originalField +
                "extraValue=" + extraValue +
                '}';
    }

    public CompareCondition(String condition, String column, Object value, Class<?> originalClass, Field originalField) {
        this.condition = condition;
        this.column = column;
        this.value = value;
        this.originalClass = originalClass;
        this.originalField = originalField;
    }

    public CompareCondition(String condition, String column, Object value, Class<?> originalClass, Field originalField,Object extraValue) {
        this.condition = condition;
        this.column = column;
        this.value = value;
        this.originalClass = originalClass;
        this.originalField = originalField;
        this.extraValue = extraValue;
    }

    public CompareCondition(String condition, Object value,Class<?> originalClass, Field originalField){
        this.condition = condition;
        this.value = value;
        this.originalClass = originalClass;
        this.originalField = originalField;
    }

    public CompareCondition(String condition, Object value,Class<?> originalClass, Field originalField,Object extraValue){
        this.condition = condition;
        this.value = value;
        this.originalClass = originalClass;
        this.originalField = originalField;
        this.extraValue = extraValue;
    }

    public CompareCondition(String condition, List<CompareCondition> compareConditionList){
        this.condition = condition;
        this.value = compareConditionList;
    }

    public CompareCondition() {
    }

    public static class CompareConditionBuilder {
        private String condition;
        private String column;
        private Object value;
        private Class<?> originalClass;
        private Field originalField;
        CompareConditionBuilder() {
        }

        public CompareConditionBuilder condition(String condition) {
            this.condition = condition;
            return this;
        }

        public CompareConditionBuilder column(String column) {
            this.column = column;
            return this;
        }

        public CompareConditionBuilder value(Object value) {
            this.value = value;
            return this;
        }

        public CompareConditionBuilder originalClass(Class<?> originalClass) {
            this.originalClass = originalClass;
            return this;
        }

        public CompareConditionBuilder originalField(Field originalField) {
            this.originalField = originalField;
            return this;
        }

        public CompareCondition build() {
            return new CompareCondition(this.condition, this.column, this.value,this.originalClass, this.originalField);
        }

        @Override
        public String toString() {
            return "{" +
                    "condition='" + condition + '\'' +
                    "column='" + column + '\'' +
                    "value=" + value +
                    "originalClass=" + originalClass +
                    "originalField=" + originalField +
                    '}';
        }
    }

}
