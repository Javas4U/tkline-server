package com.bytelab.tkline.server.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 * <p>
 * 角色说明：
 * - SUPER_ADMIN: 超级管理员（最高权限）
 * - ADMIN: 普通管理员（中等权限）
 * - USER: 普通用户（基础权限）
 * <p>
 * 权限层级：SUPER_ADMIN > ADMIN > USER
 * <p>
 * 创建日期：2025-10-29
 *
 * @author apex-tunnel
 */
@Getter
public enum UserRole {
    
    /**
     * 超级管理员
     */
    SUPER_ADMIN("SUPER_ADMIN", "超级管理员", 100),
    
    /**
     * 普通管理员
     */
    ADMIN("ADMIN", "普通管理员", 50),
    
    /**
     * 普通用户
     */
    USER("USER", "普通用户", 10);
    
    /**
     * 角色编码
     */
    private final String code;
    
    /**
     * 中文显示名称
     */
    private final String description;
    
    /**
     * 权限等级（数值越大权限越高）
     */
    private final int level;
    
    UserRole(String code, String description, int level) {
        this.code = code;
        this.description = description;
        this.level = level;
    }
    
    /**
     * 根据角色编码获取枚举值
     *
     * @param code 角色编码
     * @return 枚举值
     * @throws IllegalArgumentException 如果编码不存在
     */
    public static UserRole fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("角色编码不能为空");
        }
        
        for (UserRole role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        
        throw new IllegalArgumentException("未知的角色编码: " + code);
    }
    
    /**
     * 判断当前角色是否可以管理目标角色
     * <p>
     * 规则：只能管理低等级角色
     *
     * @param targetRole 目标角色
     * @return true表示可以管理
     */
    public boolean canManage(UserRole targetRole) {
        if (targetRole == null) {
            return false;
        }
        // 只能管理等级低于自己的角色
        return this.level > targetRole.level;
    }
    
    /**
     * 判断是否为管理员角色
     *
     * @return true表示是管理员
     */
    public boolean isAdmin() {
        return this == SUPER_ADMIN || this == ADMIN;
    }
    
    /**
     * 判断是否为超级管理员
     *
     * @return true表示是超级管理员
     */
    public boolean isSuperAdmin() {
        return this == SUPER_ADMIN;
    }
}

