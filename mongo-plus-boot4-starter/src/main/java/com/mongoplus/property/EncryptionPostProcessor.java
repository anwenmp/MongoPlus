package com.mongoplus.property;

import com.mongoplus.annotation.comm.NonNull;
import com.mongoplus.domain.MongoPlusEncryptException;
import com.mongoplus.encryptor.AESExample;
import com.mongoplus.toolkit.StringUtils;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.env.PropertySourceInfo;
import org.springframework.boot.origin.Origin;
import org.springframework.boot.origin.OriginLookup;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

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
    public void postProcessEnvironment(ConfigurableEnvironment environment, @NonNull SpringApplication application) {
        String secretKey = environment.getProperty("mpw.key");

        // 如果 secretKey 为空，直接返回
        if (StringUtils.isBlank(secretKey)) {
            return;
        }

        MutablePropertySources propertySources = environment.getPropertySources();
        propertySources.stream()
                .filter(OriginTrackedMapPropertySource.class::isInstance)
                .map(OriginTrackedMapPropertySource.class::cast)
                .forEach(source -> propertySources.replace(source.getName(),
                        new DecryptingPropertySource(source, secretKey)));
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

    /**
     * 保留 Boot Binder 所需的配置键枚举，并从原配置源读取属性值和来源。
     */
    private final class DecryptingPropertySource extends MapPropertySource
            implements OriginLookup<String>, PropertySourceInfo {

        private final OriginTrackedMapPropertySource delegate;

        private final String secretKey;

        private DecryptingPropertySource(OriginTrackedMapPropertySource delegate, String secretKey) {
            super(delegate.getName(), delegate.getSource());
            this.delegate = delegate;
            this.secretKey = secretKey;
        }

        @Override
        public Object getProperty(String name) {
            Object property = delegate.getProperty(name);
            if (property == null) {
                return null;
            }

            String value = property.toString();
            if (!detected(value)) {
                return property;
            }

            try {
                return AESExample.decrypt(unWrapper(value), secretKey);
            } catch (Exception e) {
                throw new MongoPlusEncryptException("Configuration decryption failed", e);
            }
        }

        @Override
        public Origin getOrigin(String name) {
            return delegate.getOrigin(name);
        }

        @Override
        public boolean isImmutable() {
            return delegate.isImmutable();
        }
    }
}
