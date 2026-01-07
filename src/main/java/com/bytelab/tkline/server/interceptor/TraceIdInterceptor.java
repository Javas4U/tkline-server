package com.bytelab.tkline.server.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * 请求追踪ID拦截器
 * <p>
 * 功能：
 * - 为每个请求生成唯一的TraceId
 * - 将TraceId放入MDC（Mapped Diagnostic Context）
 * - 所有日志自动包含TraceId
 * - 响应头返回TraceId
 * <p>
 * 日志格式：
 * [TraceId: abc123] 用户登录，username: testuser
 * [TraceId: abc123] 密码验证成功
 * [TraceId: abc123] 登录成功
 * <p>
 * 优势：
 * - 可以根据TraceId追踪单个请求的完整处理流程
 * - 方便排查问题和性能分析
 * - 支持分布式追踪（微服务场景）
 */
@Slf4j
@Component
public class TraceIdInterceptor implements HandlerInterceptor {

    /**
     * MDC中TraceId的key
     */
    private static final String TRACE_ID_KEY = "traceId";
    
    /**
     * HTTP请求头中TraceId的key
     */
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // 1. 尝试从请求头获取TraceId（支持前端传递）
        String traceId = request.getHeader(TRACE_ID_HEADER);
        
        // 2. 如果请求头没有，生成新的TraceId
        if (traceId == null || traceId.isEmpty()) {
            traceId = generateTraceId();
        }
        
        // 3. 放入MDC（所有日志自动包含）
        MDC.put(TRACE_ID_KEY, traceId);
        
        // 4. 放入请求属性（供Controller/Service使用）
        request.setAttribute(TRACE_ID_KEY, traceId);
        
        // 5. 响应头返回TraceId（前端可用于问题反馈）
        response.setHeader(TRACE_ID_HEADER, traceId);
        
        log.debug("请求开始，TraceId: {}, URI: {}, Method: {}", 
                traceId, request.getRequestURI(), request.getMethod());
        
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                @NonNull Object handler, @Nullable Exception ex) {
        // 请求完成后清除MDC（避免内存泄漏）
        String traceId = MDC.get(TRACE_ID_KEY);
        
        log.debug("请求完成，TraceId: {}, Status: {}", traceId, response.getStatus());
        
        MDC.remove(TRACE_ID_KEY);
    }
    
    /**
     * 生成TraceId
     * <p>
     * 格式：UUID（去掉横杠）
     * 示例：a1b2c3d4e5f67890a1b2c3d4e5f67890
     * <p>
     * 可选格式：
     * - 时间戳 + 随机数
     * - Snowflake算法
     * - UUID
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 获取当前请求的TraceId
     * <p>
     * 供Controller/Service层使用
     */
    public static String getCurrentTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }
}

