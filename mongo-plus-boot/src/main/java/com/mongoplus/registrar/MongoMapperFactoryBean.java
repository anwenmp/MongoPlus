package com.mongoplus.registrar;

import com.mongoplus.mapper.BaseMapper;
import com.mongoplus.proxy.MapperProxy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author anwen
 */
public class MongoMapperFactoryBean<T> implements FactoryBean<T>, BeanFactoryAware {

    private BaseMapper baseMapper;

    private Class<T> mapperInterface;

    private Object instance;

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
        if(instance != null){
            return (T) instance;
        }
        return (T) MapperProxy.wrap(baseMapper,mapperInterface);
    }

    @Override
    public Class<?> getObjectType() {
        return mapperInterface;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.baseMapper = beanFactory.getBean(BaseMapper.class);
        try {
            this.instance = beanFactory.getBean(mapperInterface);
        } catch (Exception ignored) {}
    }

}
