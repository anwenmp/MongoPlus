package com.mongoplus.property;

import com.mongoplus.model.LogicProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 逻辑删除配置
 *
 * @author loser
 * @date 2024/4/28
 */
@ConfigurationProperties(prefix = "mongo-plus.configuration.logic")
public class MongoLogicDelProperty extends LogicProperty {

}
