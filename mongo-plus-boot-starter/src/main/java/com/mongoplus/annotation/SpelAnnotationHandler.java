package com.mongoplus.annotation;

import com.mongoplus.handlers.collection.AnnotationHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;

import java.util.function.Function;

/**
 * Spel注解处理器
 *
 * @author anwen
 */
public class SpelAnnotationHandler implements AnnotationHandler {

    public static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = SpelAnnotationSupport.PARAMETER_NAME_DISCOVERER;

    public static final ExpressionParser EXPRESSION_PARSER = SpelAnnotationSupport.EXPRESSION_PARSER;

    public final ApplicationContext applicationContext;

    public SpelAnnotationHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public <T, R> R getProperty(T obj, Function<? super T, ? extends R> func) {
        return SpelAnnotationSupport.getProperty(applicationContext, obj, func);
    }
}
