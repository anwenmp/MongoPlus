package com.mongoplus.encryptor;

import com.mongoplus.cache.global.PropertyCache;
import com.mongoplus.toolkit.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

import static com.mongoplus.toolkit.StringUtils.bytesToHex;
import static com.mongoplus.toolkit.StringUtils.hexToBytes;

/**
 * PEB混合算法
 *
 * @author anwen
 */
public class PBEExample implements Encryptor {

    private final String algorithm;

    public PBEExample(String algorithm) {
        this.algorithm = algorithm;
    }

    private static final int ITERATION_COUNT = 1000;

    private static final int SALT_LENGTH = 8;

    // 生成随机盐
    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return salt;
    }

    @Override
    public String encrypt(String data, String key, String publicKey) throws Exception {
        byte[] salt = generateSalt();

        Cipher cipher = createCipher(Cipher.ENCRYPT_MODE, key, salt);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // 盐值放在密文前面
        byte[] encryptedWithSalt = new byte[salt.length + encryptedBytes.length];
        System.arraycopy(salt, 0, encryptedWithSalt, 0, salt.length);
        System.arraycopy(
                encryptedBytes,
                0,
                encryptedWithSalt,
                salt.length,
                encryptedBytes.length
        );

        return bytesToHex(encryptedWithSalt);
    }

    @Override
    public String decrypt(String data, String key, String privateKey) throws Exception {
        byte[] encryptedWithSalt = hexToBytes(data);

        if (encryptedWithSalt.length <= SALT_LENGTH) {
            throw new IllegalArgumentException("加密数据格式不正确");
        }

        byte[] salt = Arrays.copyOfRange(
                encryptedWithSalt,
                0,
                SALT_LENGTH
        );

        byte[] encryptedBytes = Arrays.copyOfRange(
                encryptedWithSalt,
                SALT_LENGTH,
                encryptedWithSalt.length
        );

        Cipher cipher = createCipher(Cipher.DECRYPT_MODE, key, salt);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    /**
     * 创建并初始化加密器。
     *
     * @param mode Cipher.ENCRYPT_MODE 或 Cipher.DECRYPT_MODE
     * @param key  加密密钥，为空时使用默认配置
     * @param salt 盐值
     */
    private Cipher createCipher(int mode, String key, byte[] salt) throws Exception {
        String actualKey = StringUtils.isBlank(key)
                ? PropertyCache.key
                : key;

        PBEKeySpec keySpec = new PBEKeySpec(actualKey.toCharArray());

        try {
            SecretKeyFactory keyFactory =
                    SecretKeyFactory.getInstance(algorithm);

            SecretKey secretKey = keyFactory.generateSecret(keySpec);

            PBEParameterSpec parameterSpec =
                    new PBEParameterSpec(salt, ITERATION_COUNT);

            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(mode, secretKey, parameterSpec);

            return cipher;
        } finally {
            // 清除PBEKeySpec内部保存的密码字符
            keySpec.clearPassword();
        }
    }

}
