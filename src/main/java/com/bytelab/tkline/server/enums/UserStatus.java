package com.bytelab.tkline.server.enums;

import lombok.Getter;

/**
 * 用户账号状态枚举
 * <p>
 * 状态说明：
 * - NORMAL: 正常状态（可以正常使用系统）
 * - BLACKLISTED: 黑名单状态（账号被限制，禁止访问）
 * - INACTIVE: 未激活状态（新注册用户，未完成邮箱验证）
 * - SUSPENDED: 暂停状态（临时冻结，可恢复）
 * <p>
 * 创建日期：2025-10-29
 *
 * @author apex-tunnel
 */
@Getter
public enum UserStatus {
    
    /**
     * 正常状态
     */
    NORMAL("NORMAL", "正常"),
    
    /**
     * 黑名单状态
     */
    BLACKLISTED("BLACKLISTED", "黑名单"),
    
    /**
     * 未激活状态
     */
    INACTIVE("INACTIVE", "未激活"),
    
    /**
     * 暂停状态
     */
    SUSPENDED("SUSPENDED", "已暂停");
    
    /**
     * 状态编码
     */
    private final String code;
    
    /**
     * 中文显示名称
     */
    private final String description;
    
    UserStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据状态编码获取枚举值
     *
     * @param code 状态编码
     * @return 枚举值
     * @throws IllegalArgumentException 如果编码不存在
     */
    public static UserStatus fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("状态编码不能为空");
        }
        
        for (UserStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        
        throw new IllegalArgumentException("未知的状态编码: " + code);
    }
    
    /**
     * 判断账号是否可用
     *
     * @return true表示可用
     */
    public boolean isActive() {
        return this == NORMAL;
    }
    
    /**
     * 判断账号是否被限制
     *
     * @return true表示被限制
     */
    public boolean isRestricted() {
        return this == BLACKLISTED || this == SUSPENDED;
    }
}

