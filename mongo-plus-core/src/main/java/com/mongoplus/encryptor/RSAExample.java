package com.mongoplus.encryptor;

import com.mongoplus.cache.global.PropertyCache;
import com.mongoplus.toolkit.StringUtils;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import static com.mongoplus.toolkit.EncryptorUtil.getPrivateKeyFromString;
import static com.mongoplus.toolkit.EncryptorUtil.getPublicKeyFromString;

/**
 * RSA非对称加密
 *
 * @author anwen
 */
public class RSAExample implements Encryptor {

    private final String ALGORITHM = "RSA";

    /**
     * RSA加密
     * @param data 明文
     * @param publicKey 公钥
     * @return {@link byte[]}
     * @author anwen
     */
    public String encrypt(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return StringUtils.bytesToHex(cipher.doFinal(data.getBytes()));
    }

    @Override
    public String encrypt(String data, String key,String publicKey) throws Exception {
        if (StringUtils.isBlank(publicKey)){
            publicKey = PropertyCache.publicKey;
        }
        return encrypt(data,getPublicKeyFromString(publicKey,ALGORITHM));
    }

    /**
     * RSA解密
     * @param encryptedData 密文
     * @param privateKey 私钥
     * @return {@link java.lang.String}
     * @author anwen
     */
    public String decrypt(String encryptedData, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(StringUtils.hexToBytes(encryptedData)));
    }

    @Override
    public String decrypt(String encryptedData, String key, String privateKey) throws Exception {
        if (StringUtils.isBlank(privateKey)){
            privateKey = PropertyCache.publicKey;
        }
        return decrypt(encryptedData,getPrivateKeyFromString(privateKey,ALGORITHM));
    }

    /**
     * 生成RSA密钥对
     * @return {@link java.security.KeyPair}
     * @author anwen
     */
    public KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
        keyGen.initialize(2048); // 可选：1024, 2048, 4096
        return keyGen.generateKeyPair();
    }
}
