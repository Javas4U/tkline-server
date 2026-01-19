package com.bytelab.tkline.server.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * RSA 加密解密工具类
 * 提供RSA密钥对生成、加密、解密等功能
 */
@Slf4j
public class RsaKeyUtil {

    /**
     * 加密算法
     */
    private static final String ALGORITHM = "RSA";

    /**
     * 加密填充方式
     * PKCS1Padding: 更通用，兼容性好
     * OAEPWithSHA-256AndMGF1Padding: 更安全，推荐使用
     */
    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    /**
     * 密钥长度（位）
     * 推荐使用2048位或4096位
     */
    private static final int DEFAULT_KEY_SIZE = 2048;

    /**
     * RSA加密最大明文长度（字节）
     * 对于2048位密钥：245字节（2048/8 - 11）
     * 对于1024位密钥：117字节（1024/8 - 11）
     */
    private static final int MAX_ENCRYPT_BLOCK_2048 = 245;

    /**
     * RSA解密最大密文长度（字节）
     * 对于2048位密钥：256字节（2048/8）
     * 对于1024位密钥：128字节（1024/8）
     */
    private static final int MAX_DECRYPT_BLOCK_2048 = 256;

    /**
     * 生成RSA密钥对
     *
     * @param keySize 密钥长度（位），推荐2048或4096
     * @return Map，包含 publicKey 和 privateKey（Base64编码）
     */
    public static Map<String, String> generateKeyPair(int keySize) {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGen.initialize(keySize, new SecureRandom());
            KeyPair keyPair = keyPairGen.generateKeyPair();

            // 获取公钥和私钥
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            // Base64编码
            String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());

            Map<String, String> keyMap = new HashMap<>();
            keyMap.put("publicKey", publicKeyBase64);
            keyMap.put("privateKey", privateKeyBase64);

            log.info("成功生成{}位RSA密钥对", keySize);
            return keyMap;

        } catch (NoSuchAlgorithmException e) {
            log.error("生成RSA密钥对失败：算法不存在", e);
            throw new RuntimeException("生成RSA密钥对失败", e);
        }
    }

    /**
     * 生成默认长度（2048位）的RSA密钥对
     *
     * @return Map，包含 publicKey 和 privateKey（Base64编码）
     */
    public static Map<String, String> generateKeyPair() {
        return generateKeyPair(DEFAULT_KEY_SIZE);
    }

    /**
     * 公钥加密
     *
     * @param plainText      明文
     * @param publicKeyBase64 Base64编码的公钥
     * @return Base64编码的密文
     */
    public static String encrypt(String plainText, String publicKeyBase64) {
        try {
            // 解码公钥
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            // 加密
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));

            // Base64编码
            return Base64.getEncoder().encodeToString(encryptedBytes);

        } catch (Exception e) {
            log.error("RSA加密失败", e);
            throw new RuntimeException("RSA加密失败", e);
        }
    }

    /**
     * 私钥解密
     *
     * @param encryptedText    Base64编码的密文
     * @param privateKeyBase64 Base64编码的私钥
     * @return 明文
     */
    public static String decrypt(String encryptedText, String privateKeyBase64) {
        try {
            // 解码私钥
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // 解密
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes, "UTF-8");

        } catch (Exception e) {
            log.error("RSA解密失败", e);
            throw new RuntimeException("RSA解密失败", e);
        }
    }

    /**
     * 公钥分段加密（用于超长文本）
     * 将明文分段加密，适用于超过最大加密块大小的数据
     *
     * @param plainText      明文
     * @param publicKeyBase64 Base64编码的公钥
     * @return Base64编码的密文
     */
    public static String encryptLongText(String plainText, String publicKeyBase64) {
        try {
            byte[] data = plainText.getBytes("UTF-8");
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            int inputLen = data.length;
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            int offset = 0;
            byte[] cache;
            int i = 0;

            // 对数据分段加密
            while (inputLen - offset > 0) {
                if (inputLen - offset > MAX_ENCRYPT_BLOCK_2048) {
                    cache = cipher.doFinal(data, offset, MAX_ENCRYPT_BLOCK_2048);
                } else {
                    cache = cipher.doFinal(data, offset, inputLen - offset);
                }
                out.write(cache, 0, cache.length);
                i++;
                offset = i * MAX_ENCRYPT_BLOCK_2048;
            }
            byte[] encryptedData = out.toByteArray();
            out.close();

            return Base64.getEncoder().encodeToString(encryptedData);

        } catch (Exception e) {
            log.error("RSA分段加密失败", e);
            throw new RuntimeException("RSA分段加密失败", e);
        }
    }

    /**
     * 私钥分段解密（用于超长密文）
     * 将密文分段解密，对应分段加密的逆操作
     *
     * @param encryptedText    Base64编码的密文
     * @param privateKeyBase64 Base64编码的私钥
     * @return 明文
     */
    public static String decryptLongText(String encryptedText, String privateKeyBase64) {
        try {
            byte[] encryptedData = Base64.getDecoder().decode(encryptedText);
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            int inputLen = encryptedData.length;
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            int offset = 0;
            byte[] cache;
            int i = 0;

            // 对数据分段解密
            while (inputLen - offset > 0) {
                if (inputLen - offset > MAX_DECRYPT_BLOCK_2048) {
                    cache = cipher.doFinal(encryptedData, offset, MAX_DECRYPT_BLOCK_2048);
                } else {
                    cache = cipher.doFinal(encryptedData, offset, inputLen - offset);
                }
                out.write(cache, 0, cache.length);
                i++;
                offset = i * MAX_DECRYPT_BLOCK_2048;
            }
            byte[] decryptedData = out.toByteArray();
            out.close();

            return new String(decryptedData, "UTF-8");

        } catch (Exception e) {
            log.error("RSA分段解密失败", e);
            throw new RuntimeException("RSA分段解密失败", e);
        }
    }

    /**
     * 验证密钥对是否匹配
     *
     * @param publicKeyBase64  Base64编码的公钥
     * @param privateKeyBase64 Base64编码的私钥
     * @return true-匹配，false-不匹配
     */
    public static boolean verifyKeyPair(String publicKeyBase64, String privateKeyBase64) {
        try {
            String testMessage = "RSA Key Pair Verification Test";
            String encrypted = encrypt(testMessage, publicKeyBase64);
            String decrypted = decrypt(encrypted, privateKeyBase64);
            return testMessage.equals(decrypted);
        } catch (Exception e) {
            log.error("密钥对验证失败", e);
            return false;
        }
    }
}

