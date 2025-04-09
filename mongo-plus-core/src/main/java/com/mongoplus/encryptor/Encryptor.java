package com.mongoplus.encryptor;

/**
 * 加密接口
 * @author anwen
 */
public interface Encryptor {

    /**
     * 加密
     * @param data 明文
     * @param key key
     * @param publicKey 公钥
     * @return {@link java.lang.String}
     * @author anwen
     */
    String encrypt(String data,String key,String publicKey) throws Exception;

    /**
     * 解密
     *
     * @param data       密文
     * @param key        key
     * @param privateKey 私钥
     * @return {@link java.lang.String}
     * @author anwen
     */
    String decrypt(String data,String key,String privateKey) throws Exception;

}
