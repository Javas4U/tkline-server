package com.bytelab.tkline.server.filter;

import com.bytelab.tkline.server.dto.user.UserInfoDTO;
import com.bytelab.tkline.server.service.user.UserCacheService;
import com.bytelab.tkline.server.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 和 API Key 统一认证过滤器（Spring Security集成版）
 * <p>
 * 功能：
 * 1. 支持两种认证方式：
 *    - JWT Token 认证：用于用户登录后的API访问
 *    - API Key 认证：用于外部服务（如 singbox-config-updater）访问特定接口
 * 2. JWT 认证流程：
 *    - 从HTTP Header中提取JWT Token
 *    - 验证Token有效性（使用JwtUtil）
 *    - 从Token提取用户信息（userId和username）
 *    - 从缓存获取完整用户信息（UserInfoDTO，包含role等）
 *    - 创建Spring Security的Authentication对象（Principal存储完整UserInfoDTO）
 *    - 设置到SecurityContext（供@PreAuthorize和业务代码使用）
 * 3. API Key 认证流程：
 *    - 检查请求路径是否需要API Key认证
 *    - 从Authorization头提取Bearer Token
 *    - 验证API Key是否匹配配置的密钥
 *    - 认证失败返回401错误
 * 4. 自动续期：如果Token即将过期（剩余时间<总时长1/6），自动刷新并返回新Token
 * <p>
 * 架构设计：
 * - 继承OncePerRequestFilter，集成到Spring Security过滤器链
 * - Principal存储完整UserInfoDTO，业务代码通过SecurityUtils获取
 * - 无需额外的ThreadLocal管理，完全利用Spring Security生命周期
 * <p>
 * 自动续期机制：
 * - 通过响应头 X-New-Token 返回新Token
 * - 通过响应头 X-Token-Expires-At 返回新Token过期时间
 * - 前端需要监听这些响应头并更新本地Token
 * <p>
 * 执行顺序：
 * JwtAuthenticationFilter → Spring Security过滤器链 → @PreAuthorize检查 → Controller
 * <p>
 * 创建日期：2025-11-02
 * 更新日期：2025-11-03（添加自动续期功能，优化架构：UserInfoDTO存入Principal）
 * 更新日期：2025-01-20（合并API Key认证功能）
 *
 * @author Apex Tunnel Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserCacheService userCacheService;

    @Value("${api.service.secret}")
    private String apiServiceSecret;

    /**
     * 过滤器核心逻辑
     * <p>
     * 流程：
     * 1. 检查是否是API Key保护的路径，如果是则进行API Key认证
     * 2. 否则进行JWT Token认证
     * 3. 提取Token
     * 4. 验证Token
     * 5. 获取用户信息
     * 6. 设置SecurityContext（Authentication.principal = UserInfoDTO）
     * 7. 检查Token是否即将过期，自动续期
     * 8. 继续过滤器链
     *
     * @param request HTTP请求
     * @param response HTTP响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        try {
            // 1. 检查是否是需要 API Key 认证的路径
            if (isApiKeyProtectedPath(requestUri)) {
                if (!authenticateApiKey(request, response, requestUri)) {
                    // API Key 认证失败，直接返回，不继续过滤器链
                    return;
                }
                // API Key 认证成功，继续过滤器链
                filterChain.doFilter(request, response);
                return;
            }

            // 2. JWT Token 认证
            // 从Header提取JWT Token
            String token = extractToken(request);

            if (token != null && jwtUtil.validateToken(token)) {
                // 从Token提取用户信息
                Long userId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);

                log.debug("JWT Token验证通过，userId: {}, username: {}, URI: {}",
                         userId, username, requestUri);

                // 从缓存获取完整用户信息（包含role等）
                UserInfoDTO userInfo = userCacheService.getUserInfo(userId);

                if (userInfo != null) {
                    // 创建Spring Security的Authentication对象
                    // Principal 存储完整的 UserInfoDTO（而不是只存 username）
                    // Authorities使用用户的角色（USER、ADMIN、SUPER_ADMIN）
                    // 注意：Spring Security的hasRole()方法会自动添加ROLE_前缀，所以这里需要添加前缀
                    String roleWithPrefix = "ROLE_" + userInfo.getRole();
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userInfo,                                                    // Principal（完整的 UserInfoDTO 对象）
                        null,                                                        // Credentials（密码，JWT认证不需要）
                        Collections.singletonList(                                   // Authorities（权限列表）
                            new SimpleGrantedAuthority(roleWithPrefix)               // 使用ROLE_前缀的角色作为Authority
                        )
                    );

                    // 设置到Spring Security的SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("设置SecurityContext，userId: {}, username: {}, role: {}, authority: {}",
                             userInfo.getId(), username, userInfo.getRole(), roleWithPrefix);

                    // 检查Token是否即将过期，如果是则自动刷新
                    if (jwtUtil.isTokenExpiringSoon(token)) {
                        try {
                            String newToken = jwtUtil.refreshToken(token);
                            long newExpiresAt = System.currentTimeMillis() + 30 * 60 * 1000;

                            // 在响应头中返回新Token
                            response.setHeader("X-New-Token", newToken);
                            response.setHeader("X-Token-Expires-At", String.valueOf(newExpiresAt));

                            log.info("Token自动续期成功，userId: {}, username: {}, newToken已生成",
                                    userId, username);
                        } catch (Exception e) {
                            // 刷新失败不影响本次请求
                            log.warn("Token自动续期失败，userId: {}, 继续使用原Token", userId, e);
                        }
                    }

                } else {
                    log.warn("用户信息缓存不存在，userId: {}, 跳过认证设置", userId);
                }
            } else {
                log.debug("Token无效或未提供，跳过认证设置，URI: {}", requestUri);
            }

        } catch (Exception e) {
            // 异常不阻断请求，由Spring Security的异常处理机制处理
            log.error("JWT认证过滤器异常，URI: {}", requestUri, e);
        }

        // 继续过滤器链（无论认证成功还是失败）
        // 如果未认证，Spring Security会根据配置返回401
        filterChain.doFilter(request, response);
    }

    /**
     * 从HTTP Header提取JWT Token
     * <p>
     * 支持格式：
     * - Authorization: Bearer {token}
     * - Authorization: {token}
     *
     * @param request HTTP请求
     * @return Token字符串，未找到返回null
     */
    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        if (!StringUtils.hasText(authorization)) {
            return null;
        }

        // 去掉Bearer前缀（如果有）
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        // 直接返回token（兼容不带Bearer前缀的情况）
        return authorization;
    }

    /**
     * 判断是否是需要 API Key 保护的路径
     *
     * @param requestUri 请求 URI
     * @return 是否需要 API Key 认证
     */
    private boolean isApiKeyProtectedPath(String requestUri) {
        // 需要 API Key 认证的路径列表
        return requestUri.equals("/api/subscription/users");
    }

    /**
     * API Key 认证
     *
     * @param request HTTP请求
     * @param response HTTP响应
     * @param requestUri 请求URI
     * @return 认证是否成功
     * @throws IOException IO异常
     */
    private boolean authenticateApiKey(HttpServletRequest request,
                                       HttpServletResponse response,
                                       String requestUri) throws IOException {
        log.debug("拦截需要 API Key 认证的请求: {}", requestUri);

        // 从请求头获取 Authorization
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("API Key 认证失败: 缺少 Authorization 头或格式错误, URI: {}", requestUri);
            sendUnauthorizedResponse(response, "缺少或无效的 API Key");
            return false;
        }

        // 提取 API Key
        String requestApiKey = authHeader.substring(7); // 去掉 "Bearer " 前缀

        // 验证 API Key
        if (!apiServiceSecret.equals(requestApiKey)) {
            log.warn("API Key 认证失败: API Key 不匹配, URI: {}", requestUri);
            sendUnauthorizedResponse(response, "无效的 API Key");
            return false;
        }

        log.debug("API Key 认证成功, URI: {}", requestUri);
        return true;
    }

    /**
     * 发送 401 未授权响应
     *
     * @param response HTTP响应
     * @param message 错误消息
     * @throws IOException IO异常
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format("{\"code\":401,\"message\":\"%s\"}", message));
    }
}

