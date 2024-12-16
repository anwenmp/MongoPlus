package com.anwen.mongo.annotation;

import com.anwen.mongo.registrar.MongoMapperScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * mapper层接口，只继承了{@link com.anwen.mongo.mapper.MongoMapper}接口，而且不想创建实现类，可通过注解扫描自动创建实现类
 * @author anwen
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MongoMapperScannerRegistrar.class)
public @interface MongoMapperScan {

    /**
     * 扫描路径
     * @return {@link String[]}
     * @author anwen
     */
    String[] value() default {};

    // 暂时不提供太多配置

}
