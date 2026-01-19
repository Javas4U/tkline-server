package com.bytelab.tkline.server.enums;

/**
 * 重置密码错误类型枚举
 * 
 * 用于前端国际化处理，前端可根据errorType显示对应的国际化提示
 */
public enum ResetPasswordErrorType {
    
    /**
     * 用户不存在
     */
    USER_NOT_FOUND("user_not_found", "用户不存在"),
    
    /**
     * 邮箱不存在
     */
    EMAIL_NOT_FOUND("email_not_found", "邮箱不存在"),
    
    /**
     * 邮箱与用户名不匹配
     */
    EMAIL_NOT_MATCH("email_not_match", "邮箱与用户名不匹配"),
    
    /**
     * Token过期或无效
     */
    TOKEN_EXPIRED("token_expired", "Token过期或无效"),
    
    /**
     * Token与用户信息不匹配
     */
    TOKEN_NOT_MATCH("token_not_match", "Token与用户信息不匹配");
    
    /**
     * 错误代码（前端用于国际化）
     */
    private final String code;
    
    /**
     * 默认错误消息（中文）
     */
    private final String message;
    
    ResetPasswordErrorType(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}

