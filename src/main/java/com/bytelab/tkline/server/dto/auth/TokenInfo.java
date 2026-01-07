package com.bytelab.tkline.server.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Token信息
 * <p>
 * 缓存在Redis/内存中，用于快速验证token有效性
 * 只包含最小必要信息，不包含完整的用户信息（用户信息单独缓存）
 * <p>
 * 设计原则：
 * 1. Token轻量化 - 只存userId、角色、过期时间等核心信息
 * 2. 用户信息分离 - 详细信息在UserInfoDTO中，可跨token复用
 * 3. 支持扩展 - 可添加设备信息、IP地址等安全相关字段
 * <p>
 * 创建日期：2025-10-23
 *
 * @author apex-tunnel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 用户名（用于按用户名管理会话）
     */
    private String username;
    
    /**
     * Token过期时间
     */
    private LocalDateTime expireTime;
    
    /**
     * 用户角色列表
     */
    private Set<String> roles;
    
    /**
     * Token生成时间
     */
    private LocalDateTime createTime;
    
    /**
     * 设备信息（可选）
     * 例如：iOS 16.0, Android 13, Chrome 120
     */
    private String deviceInfo;
    
    /**
     * IP地址（可选）
     * 用于安全审计
     */
    private String ipAddress;
    
    /**
     * 检查token是否已过期
     *
     * @return true表示已过期
     */
    public boolean isExpired() {
        return expireTime != null && LocalDateTime.now().isAfter(expireTime);
    }
    
    /**
     * 获取剩余有效时间（秒）
     *
     * @return 剩余秒数，已过期返回0
     */
    public long getRemainingSeconds() {
        if (isExpired()) {
            return 0;
        }
        
        return java.time.Duration.between(LocalDateTime.now(), expireTime).getSeconds();
    }
}

