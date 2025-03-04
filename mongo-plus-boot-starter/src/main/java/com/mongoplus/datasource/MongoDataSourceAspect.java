package com.mongoplus.datasource;

import com.mongoplus.annotation.datasource.MongoDs;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.handlers.DataSourceHandler;
import com.mongoplus.toolkit.ClassTypeUtil;
import com.mongoplus.toolkit.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

import static com.mongoplus.annotation.SpelAnnotationHandler.EXPRESSION_PARSER;
import static com.mongoplus.annotation.SpelAnnotationHandler.PARAMETER_NAME_DISCOVERER;


/**
 * 多数据源切面
 *
 * @author JiaChaoYang
 **/
@Aspect
@Order(0)
public class MongoDataSourceAspect {

    @Around("@within(com.mongoplus.annotation.datasource.MongoDs) || @annotation(com.mongoplus.annotation.datasource.MongoDs)")
    public Object manageDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        // 获取方法或类上的注解
        MongoDs mongoDs = getMongoDsAnnotation(method);
        String mongoDsValue = mongoDs.value();
        if (StringUtils.isNotBlank(mongoDsValue) && mongoDsValue.contains("#")) {
            StandardEvaluationContext context =
                    new MethodBasedEvaluationContext(joinPoint, method, joinPoint.getArgs(), PARAMETER_NAME_DISCOVERER);
            mongoDsValue = EXPRESSION_PARSER
                    .parseExpression(mongoDsValue.replace("#",""))
                    .getValue(context, String.class);
        }

        if (mongoDs.dsHandler() != Void.class){
            DataSourceHandler dataSourceHandler =
                    (DataSourceHandler) ClassTypeUtil.getInstanceByClass(mongoDs.dsHandler());
            mongoDsValue = dataSourceHandler.getDataSource(mongoDsValue);
        }

        if (StringUtils.isBlank(mongoDsValue)) {
            throw new MongoPlusException("Data source not found");
        }

        DataSourceNameCache.setDataSource(mongoDsValue);

        try {
            return joinPoint.proceed();
        } finally {
            DataSourceNameCache.clear();
        }
    }

    private MongoDs getMongoDsAnnotation(Method method) {
        MongoDs mongoDs = AnnotationUtils.findAnnotation(method, MongoDs.class);

        if (mongoDs == null || StringUtils.isBlank(mongoDs.value())) {
            mongoDs = AnnotationUtils.findAnnotation(method.getDeclaringClass(), MongoDs.class);
        }

        return mongoDs;
    }

}
