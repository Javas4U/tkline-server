package com.bytelab.tkline.server.common;

import lombok.Data;

/**
 * 统一API响应结果
 * <p>
 * 响应码规范：
 * - 200: 成功
 * - 400: 业务错误（用户名已存在、密码错误等）
 * - 401: 未认证（Token无效）
 * - 403: 无权限
 * - 404: 资源不存在
 * - 409: 数据冲突
 * - 500: 系统错误
 */
@Data
public class ApiResult<T> {
    
    /**
     * 响应码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    /**
     * 成功响应（带数据和自定义消息）
     * 
     * @param data 响应数据
     * @param message 成功消息
     */
    public static <T> ApiResult<T> success(T data, String message) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResult<T> success() {
        return success(null);
    }

    /**
     * 失败响应（默认500）
     * 
     * @deprecated 使用 failure(code, message) 或抛出异常
     */
    @Deprecated
    public static <T> ApiResult<T> error(String message) {
        return failure(500, message);
    }

    /**
     * 失败响应（自定义错误码）
     * 
     * @deprecated 使用 failure(code, message) 或抛出异常
     */
    @Deprecated
    public static <T> ApiResult<T> error(Integer code, String message) {
        return failure(code, message);
    }
    
    /**
     * 失败响应（推荐使用）
     * 
     * @param code 错误码
     * @param message 错误消息
     */
    public static <T> ApiResult<T> failure(Integer code, String message) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }
    
    /**
     * 业务失败响应（400）
     * 
     * @param message 错误消息
     */
    public static <T> ApiResult<T> failure(String message) {
        return failure(400, message);
    }
}
