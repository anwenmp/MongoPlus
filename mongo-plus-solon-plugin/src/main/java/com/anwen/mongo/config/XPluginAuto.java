package com.anwen.mongo.config;

import org.noear.solon.core.AopContext;
import org.noear.solon.core.Plugin;

/**
 * 使用插件处理配置
 * @author JiaChaoYang
 **/
public class XPluginAuto implements Plugin {
    @Override
    public void start(AopContext context) throws Throwable {
        context.beanMake(MongoPlusConfiguration.class);
    }
}
