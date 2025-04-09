package com.mongoplus.mapping;

import com.mongoplus.toolkit.ClassTypeUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * type类型
 * @author anwen
 */
public abstract class TypeReference<T> implements Type {

    private Type type;

    private Class<?> clazz;

    public TypeReference() {
        Type superClass = getClass().getGenericSuperclass();
        this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    public TypeReference(Type type) {
        this.type = type;
    }

    public TypeReference(Class<?> clazz) {
        this.clazz = clazz;
    }

    public static TypeReference<Object> of(Type type) {
        return new TypeReference<Object>(type) {};
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    /**
     * 获取Type的Class
     * @author anwen
     */
    public Class<?> getClazz(){
        if (this.clazz == null){
            this.clazz = ClassTypeUtil.getClassFromType(this.type);
        }
        return this.clazz;
    }

}
