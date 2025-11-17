package com.mongoplus.model;

public class ExtraField {

    public ExtraField(String name, Object value, Class<?> type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    private String name;

    private Object value;

    private Class<?> type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "{" +
                "name:'" + name + '\'' +
                ", value:" + value +
                ", type:" + type +
                '}';
    }
}
