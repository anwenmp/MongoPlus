package com.mongoplus.proxy;

import com.mongoplus.mapper.BaseMapper;
import com.mongoplus.mapper.MongoMapper;
import com.mongoplus.mapper.MongoMapperImpl;

import java.lang.reflect.*;

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

    public static Object wrap(BaseMapper baseMapper,Class<?> mapperInterface){
        return Proxy.newProxyInstance(
                mapperInterface.getClassLoader(),
                new Class[]{ mapperInterface },
                new MapperProxy<>(baseMapper,mapperInterface)
        );
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
