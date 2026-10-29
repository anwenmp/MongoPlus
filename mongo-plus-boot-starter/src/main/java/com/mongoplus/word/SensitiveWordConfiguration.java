package com.mongoplus.word;

import com.mongoplus.handler.LoadExtraWord;
import com.mongoplus.manager.SensitiveWordManager;
import com.mongoplus.property.SensitiveWordProperty;
import com.mongoplus.property.SensitiveWordConfigurationProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

@Configuration
@ConditionalOnClass(SensitiveWordProperty.class)
@EnableConfigurationProperties
public class SensitiveWordConfiguration {

    private final SensitiveWordConfigurationProperty wordProperty;

    private final LoadExtraWord loadExtraWord;

    @Autowired
    public SensitiveWordConfiguration(SensitiveWordConfigurationProperty wordProperty,
                                      @Nullable LoadExtraWord loadExtraWord) {
        this.wordProperty = wordProperty;
        this.loadExtraWord = loadExtraWord;
        if (wordProperty instanceof SensitiveWordProperty && loadExtraWord != null) {
            ((SensitiveWordProperty) wordProperty).setDynamicLoadWord(loadExtraWord);
        }
    }

    /**
     * 保留原有构造器的二进制签名；新的绑定入口直接使用 core 模型。
     */
    @Deprecated
    public SensitiveWordConfiguration(WordProperty wordProperty, @Nullable LoadExtraWord loadExtraWord) {
        this((SensitiveWordConfigurationProperty) wordProperty, loadExtraWord);
    }

    @Bean("mongo-plus.sensitive-word-com.mongoplus.word.SensitiveWordConfiguration$WordProperty")
    @ConditionalOnMissingBean(SensitiveWordConfigurationProperty.class)
    @ConfigurationProperties(prefix = "mongo-plus.sensitive-word")
    public static WordProperty wordProperty() {
        // 兼容旧类型注入，三个公开类型共享同一个 Bean；字段和默认值仍只在 core 定义。
        return new WordProperty();
    }

    @Bean
    @ConditionalOnMissingBean(SensitiveWordManager.class)
    public SensitiveWordManager sensitiveWordManager() {
        if (wordProperty instanceof SensitiveWordProperty) {
            return new SensitiveWordManager((SensitiveWordProperty) wordProperty);
        }
        return new SensitiveWordManager(wordProperty, loadExtraWord);
    }

    /**
     * 保留旧公开类型的源码、二进制和 Bean 注入兼容性，不重复声明配置字段。
     * 绑定注解仅放在 Bean 方法上，此类型保留的原因是兼容旧应用的注入点。
     * @deprecated 使用 core 的 {@link SensitiveWordConfigurationProperty}。
     */
    @Deprecated
    public static class WordProperty extends SensitiveWordProperty {

    }

}
