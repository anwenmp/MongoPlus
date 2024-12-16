package com.anwen.mongo.proxy;

import com.anwen.mongo.mapper.BaseMapper;
import com.anwen.mongo.mapper.MongoMapper;
import com.anwen.mongo.mapper.MongoMapperImpl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author anwen
 */
public class MapperProxy<T> implements InvocationHandler {

    private final Object target;

    public MapperProxy(BaseMapper baseMapper, Class<T> mapperInterface) {
        MongoMapperImpl<T> mongoMapper = new MongoMapperImpl<>();
        mongoMapper.setBaseMapper(baseMapper);
        mongoMapper.setClazz(getGenericClass(mapperInterface));
        this.target = mongoMapper;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(target,args);
    }

    private Class<?> getGenericClass(Class<?> clazz) {
        Type[] types = clazz.getGenericInterfaces();
        for(Type type : types) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type rawType = parameterizedType.getRawType();
                if (rawType instanceof Class && MongoMapper.class.isAssignableFrom((Class<?>) rawType)) {
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    return (Class<?>) actualTypeArguments[0];
                }
            }
        }
        return null;
    }

}
