package com.bytelab.tkline.server.enums;

/**
 * 验证码用途类型枚举
 */
public enum VerificationCodeType {
    
    /**
     * 邮箱验证码登录
     */
    LOGIN("login", "邮箱验证码登录"),
    
    /**
     * 重置密码
     */
    RESET_PASSWORD("reset_password", "重置密码"),
    
    /**
     * 绑定邮箱
     */
    BIND_EMAIL("bind_email", "绑定邮箱"),
    
    /**
     * 修改邮箱
     */
    CHANGE_EMAIL("change_email", "修改邮箱");
    
    private final String code;
    private final String description;
    
    VerificationCodeType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
}

