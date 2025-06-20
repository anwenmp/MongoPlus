package com.mongoplus.word;

import com.mongoplus.handler.DynamicLoadWord;
import com.mongoplus.manager.SensitiveWordManager;
import com.mongoplus.property.SensitiveWordProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

@Configuration
@ConditionalOnClass(SensitiveWordProperty.class)
@EnableConfigurationProperties(SensitiveWordConfiguration.WordProperty.class)
public class SensitiveWordConfiguration {

    private final WordProperty wordProperty;

    public SensitiveWordConfiguration(WordProperty wordProperty,@Nullable DynamicLoadWord dynamicLoadWord) {
        this.wordProperty = wordProperty;
        if (dynamicLoadWord != null) {
            wordProperty.setDynamicLoadWord(dynamicLoadWord);
        }
    }

    @Bean
    @ConditionalOnMissingBean(SensitiveWordManager.class)
    public SensitiveWordManager sensitiveWordManager() {
        return new SensitiveWordManager(wordProperty);
    }

    @ConfigurationProperties(prefix = "mongo-plus.sensitive-word")
    public static class WordProperty extends SensitiveWordProperty {

    }

}
