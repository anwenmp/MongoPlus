package com.mongoplus.scanner;

import com.mongoplus.toolkit.CollUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author anwen
 */
public class MongoEntityScanner {

    private final List<String> basePackages;

    public MongoEntityScanner(List<String> basePackages) {
        this.basePackages = basePackages;
    }

    @SafeVarargs
    public final Set<Class<?>> scan(Class<? extends Annotation>... annotations) {
        if (CollUtil.isEmpty(basePackages)) {
            return new LinkedHashSet<>();
        }
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        for (Class<? extends Annotation> annotationType : annotations) {
            scanner.addIncludeFilter(new AnnotationTypeFilter(annotationType));
        }
        Set<Class<?>> entitySet = new LinkedHashSet<>();
        for (String basePackage : basePackages) {
            if (StringUtils.hasText(basePackage)) {
                for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
                    try {
                        entitySet.add(Class.forName(candidate.getBeanClassName()));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return entitySet;
    }

}
