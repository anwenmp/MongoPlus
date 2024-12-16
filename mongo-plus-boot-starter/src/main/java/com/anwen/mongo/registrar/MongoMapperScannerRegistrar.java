package com.anwen.mongo.registrar;

import com.anwen.mongo.annotation.MongoMapperScan;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.Set;

/**
 * MongoMapperScan扫描接口
 *
 * @author anwen
 */
public class MongoMapperScannerRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, @NonNull BeanDefinitionRegistry registry) {
        // 获取注解中的扫描包路径
        Map<String, Object> attributes = metadata.getAnnotationAttributes(MongoMapperScan.class.getName());
        String[] basePackages = null;
        if (attributes != null) {
            basePackages = (String[]) attributes.get("value");
        }

        // 扫描指定包路径
        if (basePackages != null) {
            for (String basePackage : basePackages) {
                // 使用 Spring 的类扫描器来扫描包中的接口
                ClassPathMongoMapperScanner scanner = new ClassPathMongoMapperScanner(registry);
                scanner.addIncludeFilter(new MongoMapperTypeFilter());
                Set<BeanDefinitionHolder> beanDefinitionHolders = scanner.doScan(basePackage);
            }
        }
    }

    /**
     * 将代理对象的实例注册为 Bean 定义
     * @author anwen
     */
    private BeanDefinition createBeanDefinition(Object proxyInstance) {
        return BeanDefinitionBuilder
                .genericBeanDefinition(proxyInstance.getClass())
                .addConstructorArgValue(proxyInstance)
                .getBeanDefinition();
    }
}
