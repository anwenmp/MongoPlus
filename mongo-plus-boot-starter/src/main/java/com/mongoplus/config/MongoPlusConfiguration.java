package com.mongoplus.config;

import com.mongoplus.annotation.SpelAnnotationHandler;
import com.mongoplus.property.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * @author JiaChaoYang
 * 连接配置
 * @since 2023-02-09 14:27
 **/
@Import(MongoPlusPropertyConfiguration.class)
public class MongoPlusConfiguration extends MongoPlusConfigurationSupport {

    public MongoPlusConfiguration(MongoDBConnectProperty mongodbConnectProperty,
                                  MongoDBConfigurationProperty mongodbConfigurationProperty,
                                  MongoDBLogProperty mongoDBLogProperty) {
        super(mongodbConnectProperty, mongodbConfigurationProperty, mongoDBLogProperty);
    }

    /**
     * 属性绑定完成后才进入客户端及转换器初始化，避免读取尚未绑定的全局缓存。
     * 原三参数构造器保留，兼容手动创建此配置类的调用方。
     */
    @Autowired
    public MongoPlusConfiguration(MongoDBConnectProperty connectProperty,
                                  MongoDBConfigurationProperty configurationProperty,
                                  MongoDBLogProperty logProperty,
                                  MongoDBCollectionProperty collectionProperty,
                                  MongoDBFieldProperty fieldProperty,
                                  MongoLogicDelProperty logicProperty,
                                  MongoEncryptorProperty encryptorProperty,
                                  MongoSpringProperty springProperty) {
        this(connectProperty, configurationProperty, logProperty);
    }

    /**
     * SpEL 注解处理器
     * @return {@link com.mongoplus.annotation.SpelAnnotationHandler}
     * @author anwen
     */
    @Bean
    @ConditionalOnMissingBean
    public SpelAnnotationHandler spelAnnotationHandler(ApplicationContext applicationContext){
        return registerAnnotationHandler(new SpelAnnotationHandler(applicationContext));
    }

}
