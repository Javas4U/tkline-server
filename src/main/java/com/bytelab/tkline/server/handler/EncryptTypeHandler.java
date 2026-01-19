package com.bytelab.tkline.server.handler;

import com.bytelab.tkline.server.constant.SecurityConstants;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.util.StringUtils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 敏感字段加密处理器
 * 用于 MyBatis 字段级别的加密存储
 */
public class EncryptTypeHandler extends BaseTypeHandler<String> {

    private static final StringEncryptor encryptor = createEncryptor();

    /**
     * 创建加密器
     */
    private static StringEncryptor createEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        
        // 使用统一的加密配置，字段加密使用独立的密钥
        config.setPassword(SecurityConstants.Jasypt.FIELD_PASSWORD);
        config.setAlgorithm(SecurityConstants.Jasypt.ALGORITHM);
        config.setKeyObtentionIterations(SecurityConstants.Jasypt.KEY_OBTENTION_ITERATIONS);
        config.setPoolSize(SecurityConstants.Jasypt.POOL_SIZE);
        config.setProviderName(SecurityConstants.Jasypt.PROVIDER_NAME);
        config.setSaltGeneratorClassName(SecurityConstants.Jasypt.SALT_GENERATOR_CLASS);
        config.setIvGeneratorClassName(SecurityConstants.Jasypt.IV_GENERATOR_CLASS);
        config.setStringOutputType(SecurityConstants.Jasypt.STRING_OUTPUT_TYPE);
        
        encryptor.setConfig(config);
        return encryptor;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        // 存储时加密
        if (StringUtils.hasText(parameter)) {
            ps.setString(i, encryptor.encrypt(parameter));
        } else {
            ps.setString(i, parameter);
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return decrypt(value);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return decrypt(value);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return decrypt(value);
    }

    /**
     * 解密方法
     */
    private String decrypt(String encryptedValue) {
        if (StringUtils.hasText(encryptedValue)) {
            try {
                return encryptor.decrypt(encryptedValue);
            } catch (Exception e) {
                // 如果解密失败，可能是未加密的数据，直接返回
                return encryptedValue;
            }
        }
        return encryptedValue;
    }
}
