package com.bytelab.tkline.server.annotation;

import java.lang.annotation.*;

/**
 * BCrypt密码散列注解
 * 
 * 使用此注解标记的字段将在存储时自动使用BCrypt进行单向散列，
 * 读取时保持散列值不变（因为BCrypt是不可逆的）
 * 
 * 用法：
 * <pre>
 * {@code
 * @BCrypt
 * @TableField(value = "password", typeHandler = BCryptTypeHandler.class)
 * private String password;
 * }
 * </pre>
 * 
 * 特点：
 * 1. 单向散列，不可逆（无法解密）
 * 2. 自动加盐，每次散列结果不同
 * 3. 存储时自动散列，读取时保持散列值
 * 4. 验证时需要特殊处理（见BCryptTypeHandler）
 * 
 * 适用场景：
 * - 用户登录密码
 * - 交易密码
 * - 支付密码
 * - 任何需要验证但不需要还原的敏感信息
 * 
 * 注意事项：
 * 1. 数据库字段类型应为 VARCHAR(255) 或更长
 * 2. BCrypt散列值固定60个字符
 * 3. 验证密码时使用 BCryptTypeHandler.verify() 方法
 * 4. 不要对已散列的值再次散列
 * 
 * vs @Encrypted对称加密：
 * - @BCrypt: 单向散列，不可逆，用于密码
 * - @Encrypted: 对称加密，可逆，用于身份证号等
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BCrypt {
    
    /**
     * BCrypt强度（工作因子）
     * 范围：4-31，默认10
     * 
     * 说明：
     * - 强度越高，安全性越好，但计算时间越长
     * - 10: ~100ms（推荐，平衡安全和性能）
     * - 12: ~400ms（高安全场景）
     * - 4:  ~5ms（仅测试环境）
     * 
     * @return 强度值，默认10
     */
    int strength() default 10;
}

