package com.mongoplus.annotation.mapper;

import org.springframework.stereotype.Repository;

import java.lang.annotation.*;

/**
 * @author anwen
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repository
public @interface Mongo {
}
