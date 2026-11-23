package com.mongoplus.config;

import com.mongoplus.annotation.SpelAnnotationHandler;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.mapper.BaseMapper;
import com.mongoplus.property.MongoDBCollectionProperty;
import com.mongoplus.property.MongoDBConfigurationProperty;
import com.mongoplus.property.MongoDBFieldProperty;
import com.mongoplus.property.MongoDBLogProperty;
import com.mongoplus.property.MongoLogicDelProperty;
import org.springframework.boot.persistence.autoconfigure.EntityScanPackages;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * MongoPlus自动注入配置
 *
 * @author JiaChaoYang
 **/
public class MongoPlusAutoConfiguration extends MongoPlusAutoConfigurationSupport {

    public MongoPlusAutoConfiguration(MongoDBLogProperty mongodbLogProperty,
                                      MongoDBCollectionProperty mongodbCollectionProperty,
                                      MongoLogicDelProperty mongoLogicDelProperty,
                                      MongoDBConfigurationProperty mongoDBConfigurationProperty,
                                      BaseMapper baseMapper,
                                      MongoDBFieldProperty ignore,
                                      ApplicationContext applicationContext, MongoPlusClient mongoPlusClient,
                                      SpelAnnotationHandler annotationHandler) {
        super(mongodbLogProperty, mongodbCollectionProperty, mongoLogicDelProperty,
                mongoDBConfigurationProperty, baseMapper, applicationContext, mongoPlusClient);
        this.init();
    }

    @Override
    protected List<String> getEntityScanPackages() {
        return EntityScanPackages.get(applicationContext).getPackageNames();
    }
}
