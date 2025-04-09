package com.mongoplus.annotation;

import com.mongoplus.handlers.collection.AnnotationHandler;
import com.mongoplus.toolkit.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.function.Function;

/**
 * Spel注解处理器
 *
 * @author anwen
 */
public class SpelAnnotationHandler implements AnnotationHandler {

    public static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    public static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    public final ApplicationContext applicationContext;

    public SpelAnnotationHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, R> R getProperty(T obj, Function<? super T, ? extends R> func) {
        R apply = func.apply(obj);
        if (apply instanceof String){
            String value = (String) apply;
            if (needsSpelParsing(value)) {
                value = value.replace("#","");
                apply = (R) parseSpelExpression(value);
            }
        }
        return apply;
    }

    /**
     * 判断字符串是否需要解析Spel表达式
     * @param value 注解值
     * @return {@link boolean}
     * @author anwen
     */
    private boolean needsSpelParsing(String value) {
        return StringUtils.isNotBlank(value) && value.contains("#");
    }

    /**
     * 解析Spel表达式并返回结果
     * @param expression 表达式
     * @return {@link java.lang.String}
     * @author anwen
     */
    private String parseSpelExpression(String expression) {
        // 创建并缓存StandardEvaluationContext
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setBeanResolver(new BeanFactoryResolver(applicationContext));

        // 解析表达式并返回结果
        return EXPRESSION_PARSER.parseExpression(expression).getValue(context, String.class);
    }

}
