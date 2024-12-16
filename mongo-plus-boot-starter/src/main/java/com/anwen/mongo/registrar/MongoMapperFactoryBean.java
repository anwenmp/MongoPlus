package com.anwen.mongo.registrar;

import com.anwen.mongo.mapper.BaseMapper;
import com.anwen.mongo.proxy.MapperProxy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

/**
 * @author anwen
 */
public class MongoMapperFactoryBean<T> implements FactoryBean<T>, BeanFactoryAware {

    private BaseMapper baseMapper;

    private Class<T> mapperInterface;

    public Class<T> getMapperInterface() {
        return mapperInterface;
    }

    public void setMapperInterface(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public MongoMapperFactoryBean(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public MongoMapperFactoryBean(){}

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() {
        return (T) Proxy.newProxyInstance(
                mapperInterface.getClassLoader(),
                new Class<?>[] {mapperInterface},
                new MapperProxy<>(baseMapper,mapperInterface)
        );
    }

    @Override
    public Class<?> getObjectType() {
        return mapperInterface;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.baseMapper = beanFactory.getBean(BaseMapper.class);
    }

}
