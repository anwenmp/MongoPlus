package com.mongoplus.annotation.comm;

import com.mongoplus.enums.AlgorithmEnum;

import java.lang.annotation.*;

/**
 * 字段加密
 * @author anwen
 */
@Target(ElementType.FIELD)
//运行时注解
@Retention(RetentionPolicy.RUNTIME)
//表明这个注解应该被 javadoc工具记录
//生成文档
@Documented
public @interface FieldEncrypt {

    /**
     * 加密类型
     * @return {@link com.mongoplus.enums.AlgorithmEnum}
     * @author anwen
     */
    AlgorithmEnum algorithm() default AlgorithmEnum.BASE64;

    /**
     * 查询是否解密
     * 如果设置为false，那么在查询时不会进行解密
     * @author anwen
     */
    boolean findDecrypt() default true;

    /**
     * 秘钥，优先于全局参数
     * @author anwen
     */
    String key() default "";

    /**
     * 私钥，优先于全局参数
     * @author anwen
     */
    String privateKey() default "";

    /**
     * 公钥，优先于全局参数
     * @author anwen
     */
    String publicKey() default "";

    /**
     * 加密处理器，需实现{@link com.mongoplus.encryptor.Encryptor}接口
     * @author anwen
     */
    Class<?> encryptor() default Void.class;

}
