package com.bytelab.tkline.server.exception;

import lombok.Getter;

/**
 * 系统异常
 * 
 * 用于系统级别的不可预期异常，如：
 * - 数据库连接失败
 * - 第三方服务调用失败
 * - 文件IO异常
 * - 网络超时
 * 
 * 特点：
 * - 不可预期的异常
 * - 需要打印堆栈信息用于排查
 * - 给用户显示通用的错误提示
 * - HTTP状态码通常为500
 */
@Getter
public class SystemException extends RuntimeException {
    
    /**
     * 错误码（默认500）
     */
    private final Integer code;
    
    /**
     * 错误消息（给用户看的）
     */
    private final String message;
    
    /**
     * 构造函数（使用默认错误码500）
     * 
     * @param message 错误消息
     */
    public SystemException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }
    
    /**
     * 构造函数（自定义错误码）
     * 
     * @param code 错误码
     * @param message 错误消息
     */
    public SystemException(Integer code, String message) {
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
    public SystemException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
        this.message = message;
    }
}

