package com.bytelab.tkline.server.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * BCrypt密码散列TypeHandler
 * 
 * 功能：
 * - 存储时：自动使用BCrypt进行单向散列
 * - 读取时：直接返回散列值（不解密，因为不可逆）
 * - 验证时：提供静态方法验证密码
 * 
 * 使用方式：
 * <pre>
 * {@code
 * @BCrypt
 * @TableField(value = "password", typeHandler = BCryptTypeHandler.class)
 * private String password;
 * 
 * // Service层使用
 * user.setPassword("password123");  // 存储时自动散列
 * userMapper.insert(user);
 * 
 * // 验证密码
 * boolean isValid = BCryptTypeHandler.verify(
 *     "用户输入",
 *     user.getPassword()  // 从数据库读取的散列值
 * );
 * }
 * </pre>
 * 
 * 注意事项：
 * 1. 不要对已散列的值再次散列（会导致无法验证）
 * 2. BCrypt散列值长度固定60字符
 * 3. 数据库字段类型建议 VARCHAR(255)
 */
public class BCryptTypeHandler extends BaseTypeHandler<String> {

    /**
     * BCrypt编码器（线程安全）
     * 默认强度：10
     */
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    /**
     * BCrypt散列值前缀标识
     * 用于判断是否已经散列过
     */
    private static final String BCRYPT_PREFIX = "$2a$";

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        // 存储时自动散列
        if (StringUtils.hasText(parameter)) {
            // 检查是否已经是BCrypt散列值（避免重复散列）
            if (parameter.startsWith(BCRYPT_PREFIX)) {
                // 已经是散列值，直接存储
                ps.setString(i, parameter);
            } else {
                // 明文密码，进行散列
                String hashed = encoder.encode(parameter);
                ps.setString(i, hashed);
            }
        } else {
            ps.setString(i, parameter);
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 读取时直接返回散列值（不解密，因为BCrypt是单向的）
        return rs.getString(columnName);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        // 读取时直接返回散列值
        return rs.getString(columnIndex);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        // 读取时直接返回散列值
        return cs.getString(columnIndex);
    }
    
    /**
     * 验证密码
     * 
     * 这是一个静态工具方法，供Service层调用
     * 
     * @param rawPassword     原始明文密码
     * @param encodedPassword BCrypt散列值
     * @return 是否匹配
     */
    public static boolean verify(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        
        try {
            return encoder.matches(rawPassword, encodedPassword);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 手动散列密码
     * 
     * 这是一个静态工具方法，供特殊场景使用
     * 注意：正常情况下不需要手动调用，TypeHandler会自动处理
     * 
     * @param rawPassword 原始明文密码
     * @return BCrypt散列值
     */
    public static String encode(String rawPassword) {
        if (!StringUtils.hasText(rawPassword)) {
            return rawPassword;
        }
        return encoder.encode(rawPassword);
    }
}

