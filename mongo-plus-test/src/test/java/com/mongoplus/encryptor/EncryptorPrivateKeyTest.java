package com.mongoplus.encryptor;

import com.mongoplus.annotation.comm.FieldEncrypt;
import com.mongoplus.cache.global.PropertyCache;
import com.mongoplus.enums.AlgorithmEnum;
import com.mongoplus.toolkit.EncryptorUtil;
import com.mongoplus.toolkit.StringUtils;
import org.junit.After;
import org.junit.Test;

import java.security.KeyPair;

import static org.junit.Assert.assertEquals;

/**
 * 验证解密链路始终使用私钥配置，而不是错误复用公钥配置。
 */
public class EncryptorPrivateKeyTest {

    private final String originalPublicKey = PropertyCache.publicKey;
    private final String originalPrivateKey = PropertyCache.privateKey;
    private final String originalKey = PropertyCache.key;

    @After
    public void restoreGlobalKeys() {
        PropertyCache.publicKey = originalPublicKey;
        PropertyCache.privateKey = originalPrivateKey;
        PropertyCache.key = originalKey;
        EncryptorUtil.encryptorCache.remove(RecordingEncryptor.class);
    }

    @Test
    public void decryptShouldPassAnnotationPrivateKeyToEncryptor() throws Exception {
        FieldEncrypt fieldEncrypt = EncryptedEntity.class.getDeclaredField("value")
                .getAnnotation(FieldEncrypt.class);

        Object decrypted = EncryptorUtil.decrypt(fieldEncrypt, "cipher-text");

        assertEquals("cipher-text", decrypted);
        assertEquals("annotation-private-key", RecordingEncryptor.lastPrivateKey);
    }

    @Test
    public void rsaDecryptShouldFallbackToGlobalPrivateKey() throws Exception {
        RSAExample encryptor = new RSAExample();
        KeyPair keyPair = encryptor.generateKeyPair();
        String publicKey = StringUtils.bytesToHex(keyPair.getPublic().getEncoded());
        PropertyCache.publicKey = "invalid-public-key";
        PropertyCache.privateKey = StringUtils.bytesToHex(keyPair.getPrivate().getEncoded());

        String encrypted = encryptor.encrypt("mongo-plus-rsa", "", publicKey);

        assertEquals("mongo-plus-rsa", encryptor.decrypt(encrypted, "", ""));
    }

    @Test
    public void sm2DecryptShouldFallbackToGlobalPrivateKey() throws Exception {
        SM2Example encryptor = new SM2Example();
        KeyPair keyPair = encryptor.generateKeyPair();
        String publicKey = StringUtils.bytesToHex(keyPair.getPublic().getEncoded());
        PropertyCache.publicKey = "invalid-public-key";
        PropertyCache.privateKey = StringUtils.bytesToHex(keyPair.getPrivate().getEncoded());

        String encrypted = encryptor.encrypt("mongo-plus-sm2", "", publicKey);

        assertEquals("mongo-plus-sm2", encryptor.decrypt(encrypted, "", ""));
    }

    /**
     * 验证 PBE 在注解未配置 key 时，读写都回退到全局 key。
     */
    @Test
    public void pbeDecryptShouldFallbackToGlobalKey() throws Exception {
        FieldEncrypt fieldEncrypt = PbeEncryptedEntity.class.getDeclaredField("value")
                .getAnnotation(FieldEncrypt.class);
        PropertyCache.key = "global-pbe-key";

        Object encrypted = EncryptorUtil.encrypt(fieldEncrypt, "mongo-plus-pbe");

        assertEquals("mongo-plus-pbe", EncryptorUtil.decrypt(fieldEncrypt, encrypted));
    }

    private static class EncryptedEntity {
        @FieldEncrypt(
                encryptor = RecordingEncryptor.class,
                publicKey = "annotation-public-key",
                privateKey = "annotation-private-key"
        )
        private String value;
    }

    private static class PbeEncryptedEntity {
        @FieldEncrypt(algorithm = AlgorithmEnum.PBEWithMD5AndDES)
        private String value;
    }

    public static class RecordingEncryptor implements Encryptor {
        private static String lastPrivateKey;

        @Override
        public String encrypt(String data, String key, String publicKey) {
            return data;
        }

        @Override
        public String decrypt(String data, String key, String privateKey) {
            lastPrivateKey = privateKey;
            return data;
        }
    }
}
