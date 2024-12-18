package com.mongoplus.registrar;

import com.mongoplus.annotation.MongoMapperScan;
import com.mongoplus.mapper.MongoMapper;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.lang.NonNull;

import java.util.Map;

/**
 * MongoMapperScan扫描接口
 *
 * @author anwen
 */
public class MongoMapperScannerRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, @NonNull BeanDefinitionRegistry registry) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(MongoMapperScan.class.getName());
        String[] basePackages = null;
        if (attributes != null) {
            basePackages = (String[]) attributes.get("value");
        }

        if (basePackages != null) {
            for (String basePackage : basePackages) {
                ClassPathMongoMapperScanner scanner = new ClassPathMongoMapperScanner(registry);
                scanner.addIncludeFilter(new MongoMapperTypeFilter());
                scanner.scan(basePackage);
            }
        }
    }

    /**
     * MongoMapper接口的扫描过滤器，如果实现的接口不是MongoMapper，则返回false,
     * AssignableTypeFilter类默认返回null，实现的接口不是MongoMapper也会扫描到
     * @author anwen
     */
    public static class MongoMapperTypeFilter extends AssignableTypeFilter {

        public MongoMapperTypeFilter() {
            super(MongoMapper.class);
        }

        @Override
        protected Boolean matchTargetType(@NonNull String typeName) {
            Boolean _b = super.matchTargetType(typeName);
            return _b != null && _b;
        }
    }

}
