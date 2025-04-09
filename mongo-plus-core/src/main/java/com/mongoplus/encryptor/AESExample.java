package com.mongoplus.encryptor;

import com.mongoplus.cache.global.PropertyCache;
import com.mongoplus.enums.AlgorithmEnum;
import com.mongoplus.toolkit.EncryptorUtil;
import com.mongoplus.toolkit.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import static com.mongoplus.toolkit.StringUtils.hexToBytes;

/**
 * AES对称加密
 *
 * @author anwen
 */
public class AESExample implements Encryptor {

    private final String ALGORITHM = "AES";

    /**
     * 默认字符串
     */
    private static final String DEFAULT_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    
    /**
     * 默认获取大小
     */
    private static final int DEFAULT_LENGTH = 16;
    
    /**
     * 使用{@link SecureRandom}生成高质量的随机索引
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * AES加密
     * @param data 密文
     * @param password 密码
     * @return {@link byte[]}
     * @author anwen
     */
    @Override
    public String encrypt(String data, String password,String publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        if (StringUtils.isBlank(password)){
            password = PropertyCache.key;
        }
        cipher.init(Cipher.ENCRYPT_MODE, getKeyFromPassword(password));
        return StringUtils.bytesToHex(cipher.doFinal(data.getBytes()));
    }

    /**
     * AES解密
     *
     * @param encryptedData 密文
     * @param password      密码
     * @param privateKey 私钥
     * @return {@link java.lang.String}
     * @author anwen
     */
    @Override
    public String decrypt(String encryptedData, String password, String privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        if (StringUtils.isBlank(password)){
            password = PropertyCache.key;
        }
        cipher.init(Cipher.DECRYPT_MODE, getKeyFromPassword(password));
        return new String(cipher.doFinal(hexToBytes(encryptedData)));
    }

    /**
     * 生成AES密钥
     * @return {@link javax.crypto.SecretKey}
     * @author anwen
     */
    public SecretKey getKeyFromPassword(String password) throws NoSuchAlgorithmException {
        byte[] key = password.getBytes();
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        key = sha.digest(key);
        // 使用前16字节生成AES密钥
        key = Arrays.copyOf(key, 16);
        return new SecretKeySpec(key, ALGORITHM);
    }

    /**
     * 生成随机指定长度的AES秘钥
     * @param length 长度
     * @return {@link java.lang.String}
     * @author anwen
     */
    public static String generateRandom(int length){
        char[] result = new char[length];
        for (int i = 0; i< length; i++){
            result[i] = DEFAULT_CHARACTERS.charAt(SECURE_RANDOM.nextInt(DEFAULT_CHARACTERS.length()));
        }
        return new String(result);
    }

    /**
     * 生成16位随机AES秘钥
     * @return {@link java.lang.String}
     * @author anwen
     */
    public static String generateRandom(){
        return generateRandom(DEFAULT_LENGTH);
    }

    /**
     * AES加密数据
     * @param data 明文
     * @param password 秘钥
     * @return {@link java.lang.String}
     * @author anwen
     */
    public static String encrypt(String data, String password) throws Exception {
        return EncryptorUtil.algorithmEnumEncryptorMap.get(AlgorithmEnum.AES).encrypt(data,password,null);
    }

    /**
     * AES解密数据
     * @param encryptedData 密文
     * @param password 秘钥
     * @return {@link java.lang.String}
     * @author anwen
     */
    public static String decrypt(String encryptedData, String password) throws Exception {
        return EncryptorUtil.algorithmEnumEncryptorMap.get(AlgorithmEnum.AES).decrypt(encryptedData,password,null);
    }

}
