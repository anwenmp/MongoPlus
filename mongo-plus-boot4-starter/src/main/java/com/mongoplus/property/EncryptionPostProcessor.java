package com.mongoplus.property;

import com.mongoplus.annotation.comm.NonNull;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.env.PropertySourceInfo;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 数据解密
 * @author anwen
 * @since mp
 */
public class EncryptionPostProcessor extends EncryptionPostProcessorSupport implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, @NonNull SpringApplication application) {
        processEnvironment(environment);
    }

    @Override
    protected DecryptingPropertySource createPropertySource(OriginTrackedMapPropertySource source, String secretKey) {
        return new Boot4DecryptingPropertySource(source, secretKey);
    }

    private final class Boot4DecryptingPropertySource extends DecryptingPropertySource implements PropertySourceInfo {
        private Boot4DecryptingPropertySource(OriginTrackedMapPropertySource source, String secretKey) {
            super(source, secretKey);
        }
    }
}
