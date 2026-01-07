package com.bytelab.tkline.server.handler;

import com.bytelab.tkline.server.common.ApiResult;
import com.bytelab.tkline.server.exception.BusinessException;
import com.bytelab.tkline.server.exception.SystemException;
import com.bytelab.tkline.server.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 统一处理所有异常，区分业务异常和系统异常：
 * - 业务异常：返回友好的错误提示
 * - 系统异常：记录详细日志，返回通用错误提示
 * <p>
 * 异常分类：
 * 1. BusinessException：业务逻辑异常（可预期）
 * 2. SystemException：系统级异常（不可预期）
 * 3. Spring Security异常：认证和授权异常
 * 4. 框架异常：参数验证、类型转换等
 * 5. 数据库异常：SQL异常、连接异常等
 * 6. 其他未知异常
 * <p>
 * 最后修改：2025-11-02（添加Spring Security异常处理）
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Spring Security认证异常处理
     * <p>
     * 处理JWT认证失败、Token无效或过期等情况
     * <p>
     * 场景：
     * - 未携带Token
     * - Token格式错误
     * - Token已过期
     * - Token签名验证失败
     * <p>
     * 最后修改：2025-11-02
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResult<?> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.warn("认证失败，URI: {}, 错误: {}", request.getRequestURI(), e.getMessage());
        
        return ApiResult.failure(401, "认证失败，请重新登录");
    }
    
    /**
     * Spring Security授权异常处理
     * <p>
     * 处理用户权限不足的情况（@PreAuthorize检查失败）
     * <p>
     * 场景：
     * - 用户角色不满足@PreAuthorize要求
     * - 用户尝试访问无权限的资源
     * <p>
     * 审计日志：记录所有权限拒绝事件，包含：
     * - 用户名和当前角色
     * - 请求路径和HTTP方法
     * - 时间戳和TraceId
     * <p>
     * 最后修改：2025-11-02
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResult<?> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        try {
            // 获取当前用户信息（用于审计日志）
            String username = SecurityUtils.getCurrentUsername();
            String role = SecurityUtils.getCurrentUser() != null 
                ? SecurityUtils.getCurrentUser().getRole() 
                : "UNKNOWN";
            
            // 记录详细的审计日志
            log.warn("权限不足（403拒绝） - 用户: {}, 角色: {}, 路径: {}, 方法: {}, TraceId: {}", 
                     username, 
                     role, 
                     request.getRequestURI(), 
                     request.getMethod(),
                     request.getAttribute("traceId"));
                     
        } catch (Exception ex) {
            // 获取用户信息失败（可能未认证），记录基本信息
            log.warn("权限不足（403拒绝） - 路径: {}, 方法: {}, 错误: {} (用户信息获取失败)", 
                     request.getRequestURI(), 
                     request.getMethod(),
                     e.getMessage());
        }
        
        return ApiResult.failure(403, "权限不足，无法访问此资源");
    }
    
    /**
     * 业务异常处理
     * <p>
     * 特点：
     * - 可预期的业务逻辑异常
     * - 直接返回错误消息给用户
     * - 不打印堆栈（避免日志污染）
     * - HTTP 200，通过业务code区分
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常，URI: {}, 错误码: {}, 错误信息: {}", 
                request.getRequestURI(), e.getCode(), e.getMessage());
        
        return ApiResult.failure(e.getCode(), e.getMessage());
    }
    
    /**
     * 系统异常处理
     * <p>
     * 特点：
     * - 不可预期的系统级异常
     * - 打印完整堆栈用于排查
     * - 给用户显示通用错误提示
     * - HTTP 500
     */
    @ExceptionHandler(SystemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<?> handleSystemException(SystemException e, HttpServletRequest request) {
        log.error("系统异常，URI: {}, 错误码: {}, 错误信息: {}", 
                request.getRequestURI(), e.getCode(), e.getMessage(), e);
        
        return ApiResult.failure(e.getCode(), "系统繁忙，请稍后再试");
    }
    
    /**
     * 参数验证异常处理
     *
     * @Valid 注解验证失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<?> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errors = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        log.warn("参数验证失败，URI: {}, 错误: {}", request.getRequestURI(), errors);
        
        return ApiResult.failure(400, "参数验证失败：" + errors);
    }
    
    /**
     * 参数绑定异常处理
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<?> handleBindException(BindException e, HttpServletRequest request) {
        String errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        log.warn("参数绑定失败，URI: {}, 错误: {}", request.getRequestURI(), errors);
        
        return ApiResult.failure(400, "参数错误：" + errors);
    }
    
    /**
     * 缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<?> handleMissingParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.warn("缺少请求参数，URI: {}, 参数: {}", request.getRequestURI(), e.getParameterName());
        
        return ApiResult.failure(400, "缺少必需参数：" + e.getParameterName());
    }
    
    /**
     * 参数类型转换异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<?> handleTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("参数类型错误，URI: {}, 参数: {}, 期望类型: {}", 
                request.getRequestURI(), e.getName(), e.getRequiredType());
        
        return ApiResult.failure(400, "参数类型错误：" + e.getName());
    }
    
    /**
     * 请求体不可读异常（JSON格式错误）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<?> handleMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.warn("请求体格式错误，URI: {}", request.getRequestURI());
        
        return ApiResult.failure(400, "请求体格式错误，请检查JSON格式");
    }
    
    /**
     * 请求方法不支持异常（如POST用成了GET）
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResult<?> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("请求方法不支持，URI: {}, 方法: {}, 支持的方法: {}", 
                request.getRequestURI(), e.getMethod(), e.getSupportedHttpMethods());
        
        return ApiResult.failure(405, "请求方法不支持：" + e.getMethod());
    }
    
    /**
     * 数据库异常处理
     * <p>
     * 包括：
     * - SQL语法错误
     * - 唯一键冲突
     * - 外键约束
     * - 连接超时
     */
    @ExceptionHandler({SQLException.class, DataAccessException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<?> handleDatabaseException(Exception e, HttpServletRequest request) {
        log.error("数据库异常，URI: {}", request.getRequestURI(), e);
        
        // 提取有用的错误信息
        String message = e.getMessage();
        if (message != null) {
            if (message.contains("Duplicate entry")) {
                return ApiResult.failure(409, "数据已存在");
            } else if (message.contains("foreign key constraint")) {
                return ApiResult.failure(409, "数据关联冲突，无法操作");
            } else if (message.contains("timeout")) {
                return ApiResult.failure(504, "数据库连接超时");
            }
        }
        
        return ApiResult.failure(500, "数据库操作失败");
    }
    
    /**
     * 空指针异常处理
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<?> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常，URI: {}", request.getRequestURI(), e);
        
        return ApiResult.failure(500, "系统内部错误");
    }
    
    /**
     * 非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<?> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("非法参数异常，URI: {}, 错误: {}", request.getRequestURI(), e.getMessage());
        
        return ApiResult.failure(400, e.getMessage());
    }
    
    /**
     * 非法状态异常
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<?> handleIllegalStateException(IllegalStateException e, HttpServletRequest request) {
        log.warn("非法状态异常，URI: {}, 错误: {}", request.getRequestURI(), e.getMessage());
        
        return ApiResult.failure(400, e.getMessage());
    }
    
    /**
     * 安全异常（权限不足）
     */
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResult<?> handleSecurityException(SecurityException e, HttpServletRequest request) {
        log.warn("安全异常，URI: {}, 错误: {}", request.getRequestURI(), e.getMessage());
        
        return ApiResult.failure(403, e.getMessage());
    }
    
    /**
     * 乐观锁冲突异常
     */
    @ExceptionHandler(org.springframework.dao.OptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResult<?> handleOptimisticLockingFailureException(
            org.springframework.dao.OptimisticLockingFailureException e, 
            HttpServletRequest request) {
        log.warn("乐观锁冲突，URI: {}", request.getRequestURI());
        
        return ApiResult.failure(409, "数据已被其他操作更新，请刷新后重试");
    }
    
    /**
     * 其他未捕获的RuntimeException
     * <p>
     * 作为兜底处理，捕获Service层抛出的RuntimeException
     * 如果不是BusinessException或SystemException，视为业务异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResult<?> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        // 检查是否是已知的业务异常消息
        String message = e.getMessage();
        
        log.warn("运行时异常，URI: {}, 错误: {}", request.getRequestURI(), message);
        
        // 如果有明确的错误消息，返回给用户
        if (message != null && !message.isEmpty()) {
            return ApiResult.failure(400, message);
        }
        
        return ApiResult.failure(500, "操作失败");
    }
    
    /**
     * 兜底异常处理
     * <p>
     * 捕获所有未处理的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<?> handleException(Exception e, HttpServletRequest request) {
        log.error("未知异常，URI: {}", request.getRequestURI(), e);
        
        return ApiResult.failure(500, "系统错误，请联系管理员");
    }
}

