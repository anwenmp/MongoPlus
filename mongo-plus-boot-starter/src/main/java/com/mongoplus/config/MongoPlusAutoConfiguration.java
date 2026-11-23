package com.mongoplus.config;

import com.mongoplus.annotation.SpelAnnotationHandler;
import com.mongoplus.cache.global.HandlerCache;
import com.mongoplus.handlers.ReadHandler;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.mapper.BaseMapper;
import com.mongoplus.property.MongoDBCollectionProperty;
import com.mongoplus.property.MongoDBConfigurationProperty;
import com.mongoplus.property.MongoDBFieldProperty;
import com.mongoplus.property.MongoDBLogProperty;
import com.mongoplus.property.MongoLogicDelProperty;
import com.mongoplus.toolkit.CollUtil;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    @Override
    public void init() {
        super.init();
        setReadHandler();
    }

    /**
     * 设置读取处理器
     */
    public void setReadHandler() {
        Map<String, ReadHandler> readHandlerMap = applicationContext.getBeansOfType(ReadHandler.class);
        if (CollUtil.isEmpty(readHandlerMap)) {
            return;
        }
        Collection<ReadHandler> values = readHandlerMap.values();
        HandlerCache.addReadHandler(new ArrayList<>(values));
    }
}
