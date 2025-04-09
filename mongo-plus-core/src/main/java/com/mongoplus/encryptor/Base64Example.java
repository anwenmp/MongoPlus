package com.mongoplus.encryptor;

import java.util.Base64;

/**
 * base64加密
 *
 * @author anwen
 */
public class Base64Example implements Encryptor {

    @Override
    public String encrypt(String data, String key,String publicKey) throws Exception {
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    @Override
    public String decrypt(String data, String key, String privateKey) throws Exception {
        return new String(Base64.getDecoder().decode(data));
    }
}
