package com.mongoplus.annotation.logice;

import java.lang.annotation.*;


@Target({ElementType.METHOD,ElementType.TYPE})
//运行时注解
@Retention(RetentionPolicy.RUNTIME)
//表明这个注解应该被 javadoc工具记录
//生成文档
@Documented
public @interface IgnoreLogic {

}
