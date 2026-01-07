package com.bytelab.tkline.server.annotation;

import java.lang.annotation.*;

/**
 * 字段对称加密存储注解
 * 
 * ⚠️ 注意：此注解当前未实现，为预留的扩展功能
 * 
 * 使用此注解标记的字段将在数据库存储时自动加密，读取时自动解密
 * 需要配合MyBatis TypeHandler实现（如EncryptTypeHandler）
 * 
 * 用法示例：
 * 
 * <pre>
 * {
 *     &#64;code
 *     &#64;Encrypt(type = EncryptionType.AES)
 *     @TableField(value = "id_card", typeHandler = EncryptTypeHandler.class)
 *     private String idCard;
 * }
 * </pre>
 * 
 * 注意事项：
 * 1. 加密字段对应的数据库列类型应为 TEXT 或 VARCHAR(足够长)
 * 2. 加密字段无法使用数据库索引进行高效查询
 * 3. 适用于高敏感信息（如身份证号、银行卡号等）
 * 4. 不适用于需要频繁查询的字段（如邮箱、手机号）
 * 
 * 推荐加密的字段：
 * - 身份证号
 * - 银行卡号
 * - 详细地址
 * - 其他高敏感个人信息
 * 
 * 不推荐加密的字段：
 * - 邮箱（需要查询和索引）
 * - 手机号（需要查询和索引）
 * - 用户名（需要查询和索引）
 * - 密码（应使用BCrypt等单向散列，不是对称加密）
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Encrypt {

    /**
     * 加密算法类型
     * 
     * @return 算法类型，默认使用AES
     */
    EncryptionType type() default EncryptionType.AES;

    /**
     * 加密算法枚举
     */
    enum EncryptionType {
        /**
         * AES对称加密（默认）
         * 优点：速度快，标准算法，安全性高
         */
        AES,

        /**
         * 自定义加密
         * 需要实现自定义的加密解密逻辑
         */
        CUSTOM
    }
}
