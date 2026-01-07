package com.bytelab.tkline.server.exception;

import lombok.Getter;

/**
 * 业务异常
 * 
 * 用于业务逻辑处理中的可预期异常，如：
 * - 用户名已存在
 * - 密码错误
 * - 验证码错误
 * - 邮箱未注册
 * - 账户已禁用
 * 
 * 特点：
 * - 可预期的异常
 * - 可以友好提示给用户
 * - 不需要打印堆栈信息
 * - HTTP状态码通常为200，通过业务code区分
 */
@Getter
public class BusinessException extends RuntimeException {
    
    /**
     * 业务错误码
     */
    private final Integer code;
    
    /**
     * 错误消息
     */
    private final String message;
    
    /**
     * 构造函数（使用默认业务错误码400）
     * 
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
        this.message = message;
    }
    
    /**
     * 构造函数（自定义错误码）
     * 
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    /**
     * 构造函数（包含原始异常）
     * 
     * @param message 错误消息
     * @param cause 原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 400;
        this.message = message;
    }
}

