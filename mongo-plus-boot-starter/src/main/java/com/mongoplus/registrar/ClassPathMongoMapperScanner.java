package com.mongoplus.registrar;

import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.toolkit.CollUtil;
import org.springframework.aop.scope.ScopedProxyFactoryBean;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.lang.NonNull;

import java.util.Optional;
import java.util.Set;

/**
 * @author anwen
 */
@SuppressWarnings("rawtypes")
public class ClassPathMongoMapperScanner extends ClassPathBeanDefinitionScanner {

    private final Log log = LogFactory.getLog(ClassPathMongoMapperScanner.class);

    private Class<? extends MongoMapperFactoryBean> factoryBean = MongoMapperFactoryBean.class;

    static final String FACTORY_BEAN_OBJECT_TYPE = "factoryBeanObjectType";

    public ClassPathMongoMapperScanner(BeanDefinitionRegistry registry) {
        super(registry,false);
    }

    public void setFactoryBean(Class<? extends MongoMapperFactoryBean> factoryBean) {
        this.factoryBean = factoryBean;
    }

    @Override
    @NonNull
    protected Set<BeanDefinitionHolder> doScan(@NonNull String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
        if (CollUtil.isNotEmpty(beanDefinitionHolders)) {
            handleBeanDefinition(beanDefinitionHolders);
        }
        return beanDefinitionHolders;
    }

    public void handleBeanDefinition(Set<BeanDefinitionHolder> beanDefinitions) {
        BeanDefinitionRegistry registry = getRegistry();
        beanDefinitions.forEach(holder -> {
            AbstractBeanDefinition definition = (AbstractBeanDefinition) holder.getBeanDefinition();
            boolean scopedProxy = false;
            if (ScopedProxyFactoryBean.class.getName().equals(definition.getBeanClassName())) {
                definition = (AbstractBeanDefinition) Optional
                        .ofNullable(((RootBeanDefinition) definition).getDecoratedDefinition())
                        .map(BeanDefinitionHolder::getBeanDefinition).orElseThrow(() -> new IllegalStateException(
                                "The target bean definition of scoped proxy bean not found. Root bean definition[" + holder + "]"));
                scopedProxy = true;
            }
            String beanClassName = definition.getBeanClassName();
            if (beanClassName != null) {
                definition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName);
            }
            try {
                definition.getPropertyValues().add("mapperInterface", Class.forName(beanClassName));
            } catch (ClassNotFoundException ignore) {
            }
            definition.setBeanClass(factoryBean);
            definition.setAttribute(FACTORY_BEAN_OBJECT_TYPE, beanClassName);
            if (scopedProxy) {
                return;
            }
            if (!definition.isSingleton()) {
                BeanDefinitionHolder proxyHolder = ScopedProxyUtils.createScopedProxy(holder, registry, true);
                if (registry.containsBeanDefinition(proxyHolder.getBeanName())) {
                    registry.removeBeanDefinition(proxyHolder.getBeanName());
                }
                registry.registerBeanDefinition(proxyHolder.getBeanName(), proxyHolder.getBeanDefinition());
            }
        });
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

}
