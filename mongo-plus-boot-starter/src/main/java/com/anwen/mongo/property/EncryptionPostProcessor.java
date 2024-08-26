package com.anwen.mongo.property;

import com.anwen.mongo.domain.MongoPlusEncryptException;
import com.anwen.mongo.encryptor.AESExample;
import com.anwen.mongo.toolkit.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

/**
 * 数据解密
 * @author anwen
 * @since mp
 */
public class EncryptionPostProcessor implements EnvironmentPostProcessor, Ordered {

    /**
     * 前缀
     * @date 2024/8/26 13:31
     */
    private final String PREFIX = "${ENC:";

    /**
     * 后缀
     * @date 2024/8/26 13:31
     */
    private final String SUFFIX = "}";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String secretKey = environment.getProperty("mpw.key");

        // 如果 secretKey 为空，直接返回
        if (StringUtils.isBlank(secretKey)) {
            return;
        }

        environment.getPropertySources().stream()
                .filter(OriginTrackedMapPropertySource.class::isInstance)
                .forEach(propertySource -> {
                    String sourceName = propertySource.getName();
                    PropertySource<Object> encryptedPropertySource = new PropertySource<Object>(sourceName) {
                        @Override
                        public Object getProperty(String name) {
                            Object property = propertySource.getProperty(name);
                            if (property != null) {
                                String value = property.toString();
                                if (detected(value)) {
                                    try {
                                        return AESExample.decrypt(unWrapper(value), secretKey);
                                    } catch (Exception e) {
                                        throw new MongoPlusEncryptException("Configuration decryption failed", e);
                                    }
                                }
                            }
                            return property;
                        }
                    };
                    environment.getPropertySources().replace(sourceName, encryptedPropertySource);
                });
    }

    @Override
    public int getOrder() {
        return 0;
    }

    public boolean detected(String property) {
        return property != null && property.startsWith(PREFIX) && property.endsWith(SUFFIX);
    }

    public String wrapper(String property) {
        return PREFIX + property + SUFFIX;
    }

    public String unWrapper(String property) {
        return property.substring(PREFIX.length(), property.length() - SUFFIX.length());
    }
}
