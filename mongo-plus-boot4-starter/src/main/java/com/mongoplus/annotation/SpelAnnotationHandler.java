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
public record SpelAnnotationHandler(ApplicationContext applicationContext) implements AnnotationHandler {

    public static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = SpelAnnotationSupport.PARAMETER_NAME_DISCOVERER;

    public static final ExpressionParser EXPRESSION_PARSER = SpelAnnotationSupport.EXPRESSION_PARSER;

    @Override
    public <T, R> R getProperty(T obj, Function<? super T, ? extends R> func) {
        return SpelAnnotationSupport.getProperty(applicationContext, obj, func);
    }
}
