package com.mongoplus.annotation.mapper;

import org.noear.solon.annotation.Component;
import java.lang.annotation.*;

/**
 * @author anwen
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Mongo {
}
