package com.mongoplus.property;

import com.mongoplus.domain.MongoPlusEncryptException;
import com.mongoplus.encryptor.AESExample;
import com.mongoplus.toolkit.StringUtils;
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
     */
    private final String PREFIX = "${ENC:";

    /**
     * 后缀
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
                        @SuppressWarnings("NullableProblems")
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
