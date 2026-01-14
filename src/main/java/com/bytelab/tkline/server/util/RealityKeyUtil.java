package com.bytelab.tkline.server.util;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Reality 协议密钥生成工具类
 * Reality 使用 X25519 椭圆曲线加密算法
 * 生成用于 VLESS-Reality 协议的公私钥对
 */
@Slf4j
public class RealityKeyUtil {

    /**
     * X25519 密钥长度（字节）
     */
    private static final int KEY_SIZE = 32;

    /**
     * 生成 Reality 密钥对
     * Reality 使用 X25519 椭圆曲线密钥交换算法
     *
     * @return Map，包含 publicKey 和 privateKey（Base64编码）
     */
    public static Map<String, String> generateRealityKeyPair() {
        try {
            // 生成私钥（32字节随机数）
            byte[] privateKey = new byte[KEY_SIZE];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(privateKey);

            // X25519 私钥需要进行特定的位操作（clamping）
            // 参考 RFC 7748 Section 5
            privateKey[0] &= 248;  // 清除最低3位
            privateKey[31] &= 127; // 清除最高位
            privateKey[31] |= 64;  // 设置次高位

            // 计算公钥（通过私钥进行 X25519 基点标量乘法）
            byte[] publicKey = scalarMultBase(privateKey);

            // Base64 编码
            String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey);
            String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey);

            Map<String, String> keyMap = new HashMap<>();
            keyMap.put("publicKey", publicKeyBase64);
            keyMap.put("privateKey", privateKeyBase64);

            log.info("成功生成 Reality X25519 密钥对");
            return keyMap;

        } catch (Exception e) {
            log.error("生成 Reality 密钥对失败", e);
            throw new RuntimeException("生成 Reality 密钥对失败", e);
        }
    }

    /**
     * X25519 基点标量乘法
     * 计算 privateKey * basePoint 得到公钥
     * 使用 Curve25519 基点 (9, ...)
     *
     * @param privateKey 32字节私钥
     * @return 32字节公钥
     */
    private static byte[] scalarMultBase(byte[] privateKey) {
        // X25519 基点的 u 坐标为 9
        byte[] basepoint = new byte[KEY_SIZE];
        basepoint[0] = 9;

        return scalarMult(privateKey, basepoint);
    }

    /**
     * X25519 标量乘法核心算法
     * 实现 Curve25519 椭圆曲线点乘运算
     * 参考 RFC 7748
     *
     * @param scalar 标量（私钥）
     * @param point  曲线上的点（u坐标）
     * @return 计算结果（u坐标）
     */
    private static byte[] scalarMult(byte[] scalar, byte[] point) {
        // Montgomery ladder 算法实现
        long[] x1 = new long[16];
        long[] x2 = new long[16];
        long[] z2 = new long[16];
        long[] x3 = new long[16];
        long[] z3 = new long[16];
        long[] tmp0 = new long[16];
        long[] tmp1 = new long[16];

        // 解包输入点
        unpack(x1, point);

        // 初始化
        set(x2, 1);
        set(z2, 0);
        copy(x3, x1);
        set(z3, 1);

        // Montgomery ladder 主循环
        int swap = 0;
        for (int pos = 254; pos >= 0; --pos) {
            int bit = (scalar[pos / 8] >> (pos & 7)) & 1;
            swap ^= bit;
            cswap(x2, x3, swap);
            cswap(z2, z3, swap);
            swap = bit;

            // 曲线运算
            add(tmp0, x2, z2);
            sub(tmp1, x2, z2);
            add(x2, x3, z3);
            sub(z2, x3, z3);
            mult(z3, tmp0, z2);
            mult(z2, tmp1, x2);
            square(tmp0, tmp1);
            square(tmp1, tmp0);
            add(x3, z3, z2);
            sub(z2, z3, z2);
            mult(x2, tmp1, tmp0);
            sub(tmp1, tmp1, tmp0);
            square(z2, z2);
            mult121665(z3, tmp1);
            square(x3, x3);
            add(tmp0, tmp0, z3);
            mult(z3, x1, z2);
            mult(z2, tmp1, tmp0);
        }

        cswap(x2, x3, swap);
        cswap(z2, z3, swap);

        // 计算逆并打包结果
        inverse(z2, z2);
        mult(x2, x2, z2);

        byte[] result = new byte[KEY_SIZE];
        pack(result, x2);
        return result;
    }

    // ==================== 辅助函数 ====================

    private static void unpack(long[] out, byte[] in) {
        for (int i = 0; i < 16; i++) {
            out[i] = (long) (in[2 * i] & 0xff) + ((long) (in[2 * i + 1] & 0xff) << 8);
        }
        out[15] &= 0x7fff;
    }

    private static void pack(byte[] out, long[] in) {
        long[] temp = new long[16];
        copy(temp, in);
        carry(temp);
        carry(temp);
        carry(temp);

        for (int i = 0; i < 2; i++) {
            temp[0] += 38 * temp[15];
            carry(temp);
        }

        for (int i = 0; i < 16; i++) {
            out[2 * i] = (byte) (temp[i] & 0xff);
            out[2 * i + 1] = (byte) ((temp[i] >> 8) & 0xff);
        }
    }

    private static void carry(long[] elem) {
        for (int i = 0; i < 15; i++) {
            elem[i + 1] += elem[i] >> 16;
            elem[i] &= 0xffff;
        }
        elem[0] += 38 * (elem[15] >> 15);
        elem[15] &= 0x7fff;
    }

    private static void set(long[] out, long value) {
        out[0] = value;
        for (int i = 1; i < 16; i++) {
            out[i] = 0;
        }
    }

    private static void copy(long[] out, long[] in) {
        System.arraycopy(in, 0, out, 0, 16);
    }

    private static void add(long[] out, long[] a, long[] b) {
        for (int i = 0; i < 16; i++) {
            out[i] = a[i] + b[i];
        }
    }

    private static void sub(long[] out, long[] a, long[] b) {
        for (int i = 0; i < 16; i++) {
            out[i] = a[i] - b[i];
        }
    }

    private static void mult(long[] out, long[] a, long[] b) {
        long[] temp = new long[31];
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                temp[i + j] += a[i] * b[j];
            }
        }

        for (int i = 0; i < 15; i++) {
            temp[i] += 38 * temp[i + 16];
        }

        copy(out, temp);
        carry(out);
        carry(out);
    }

    private static void square(long[] out, long[] a) {
        mult(out, a, a);
    }

    private static void mult121665(long[] out, long[] a) {
        for (int i = 0; i < 16; i++) {
            out[i] = 121665 * a[i];
        }
        carry(out);
    }

    private static void cswap(long[] a, long[] b, int swap) {
        long mask = -swap;
        for (int i = 0; i < 16; i++) {
            long t = mask & (a[i] ^ b[i]);
            a[i] ^= t;
            b[i] ^= t;
        }
    }

    private static void inverse(long[] out, long[] z) {
        long[] z2 = new long[16];
        long[] z9 = new long[16];
        long[] z11 = new long[16];
        long[] z2_5_0 = new long[16];
        long[] z2_10_0 = new long[16];
        long[] z2_20_0 = new long[16];
        long[] z2_50_0 = new long[16];
        long[] z2_100_0 = new long[16];
        long[] t0 = new long[16];
        long[] t1 = new long[16];

        square(z2, z);
        square(t1, z2);
        square(t0, t1);
        mult(z9, t0, z);
        mult(z11, z9, z2);
        square(t0, z11);
        mult(z2_5_0, t0, z9);

        square(t0, z2_5_0);
        for (int i = 1; i < 5; i++) {
            square(t0, t0);
        }
        mult(z2_10_0, t0, z2_5_0);

        square(t0, z2_10_0);
        for (int i = 1; i < 10; i++) {
            square(t0, t0);
        }
        mult(z2_20_0, t0, z2_10_0);

        square(t0, z2_20_0);
        for (int i = 1; i < 20; i++) {
            square(t0, t0);
        }
        mult(t0, t0, z2_20_0);

        square(t0, t0);
        for (int i = 1; i < 10; i++) {
            square(t0, t0);
        }
        mult(z2_50_0, t0, z2_10_0);

        square(t0, z2_50_0);
        for (int i = 1; i < 50; i++) {
            square(t0, t0);
        }
        mult(z2_100_0, t0, z2_50_0);

        square(t0, z2_100_0);
        for (int i = 1; i < 100; i++) {
            square(t0, t0);
        }
        mult(t0, t0, z2_100_0);

        square(t0, t0);
        for (int i = 1; i < 50; i++) {
            square(t0, t0);
        }
        mult(t0, t0, z2_50_0);

        square(t0, t0);
        for (int i = 1; i < 5; i++) {
            square(t0, t0);
        }
        mult(out, t0, z11);
    }
}
