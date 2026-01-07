package com.bytelab.tkline.server.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 安全配置
 * <p>
 * 功能：
 * 1. 配置 SecurityFilterChain，定义哪些路径需要认证
 * 2. 禁用不需要的默认特性（formLogin、httpBasic、CSRF）
 * 3. 配置无状态Session管理（STATELESS）
 * 4. 注册JWT认证过滤器到过滤器链
 * <p>
 * 公开路径白名单：
 * - /api/health - 健康检查（用于Docker健康检查）
 * - /api/user/login - 用户登录
 * - /api/user/create - 用户注册
 * - /api/user/check-username - 检查用户名
 * - /api/user/check-email - 检查邮箱
 * - /api/user/send-code - 发送验证码
 * - /api/user/email-login - 邮箱验证码登录
 * - /api/user/reset-password - 重置密码
 * - /api/user/send-reset-link - 发送重置链接
 * - /api/user/validate-reset-token - 验证重置Token
 * - /api/user/reset-password-by-token - 通过Token重置密码
 * - /api/security/keys/public-key - 获取公钥
 * - /api/public/tutorials/** - 公开教程
 * - /api/admin/tutorials/** - 管理端教程（开发阶段临时开放）
 * - /api/admin/platforms/** - 平台管理（开发阶段临时开放）
 * - /api/admin/categories/** - 分类管理（开发阶段临时开放）
 * - /uploads/** - 上传文件访问（公开，用于教程图片等静态资源）
 * <p>
 * 创建日期：2025-11-02
 *
 * @author Apex Tunnel Team
 * @see com.bytelab.tkline.server.filter.JwtAuthenticationFilter
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final com.bytelab.tkline.server.filter.JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置 Spring Security 过滤器链
     * <p>
     * 配置项：
     * 1. CSRF禁用（使用JWT，不需要CSRF保护）
     * 2. formLogin禁用（不使用表单登录）
     * 3. httpBasic禁用（使用JWT认证）
     * 4. Session管理为STATELESS（无状态，不创建Session）
     * 5. 公开路径配置（无需认证即可访问）
     * 6. 其他路径需要认证
     * 
     * @param http HttpSecurity配置对象
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("配置 Spring Security SecurityFilterChain");

        http
                // 启用CORS配置（允许跨域请求）
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 禁用CSRF（使用JWT，不需要CSRF保护）
                .csrf(AbstractHttpConfigurer::disable)

                // 禁用表单登录（使用JWT认证）
                .formLogin(AbstractHttpConfigurer::disable)

                // 禁用HTTP Basic认证（使用JWT认证）
                .httpBasic(AbstractHttpConfigurer::disable)

                // 配置Session管理为无状态（不创建Session）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 配置URL访问权限
                .authorizeHttpRequests(auth -> auth
                        // 健康检查接口（公开，用于Docker健康检查）
                        .requestMatchers("/api/health").permitAll()

                        // 用户登录和注册相关（公开）
                        .requestMatchers("/api/user/login").permitAll()
                        .requestMatchers("/api/user/create").permitAll()
                        .requestMatchers("/api/user/check-username").permitAll()
                        .requestMatchers("/api/user/check-email").permitAll()

                        // 邮箱验证码功能（公开）
                        .requestMatchers("/api/user/send-code").permitAll()
                        .requestMatchers("/api/user/email-login").permitAll()
                        .requestMatchers("/api/user/reset-password").permitAll()

                        // 重置密码链接功能（公开）
                        .requestMatchers("/api/user/send-reset-link").permitAll()
                        .requestMatchers("/api/user/validate-reset-token").permitAll()
                        .requestMatchers("/api/user/reset-password-by-token").permitAll()

                        // 获取公钥(登录前需要,公开)
                        .requestMatchers("/api/security/keys/public-key").permitAll()

                        // 节点和订阅管理 API (开发阶段临时开放)
                        .requestMatchers("/api/v1/node/**").permitAll()
                        .requestMatchers("/api/v1/subscription/**").permitAll()
                        .requestMatchers("/api/v1/relation/**").permitAll()

                        // Swagger文档(开发环境,生产环境建议禁用)
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        // 其他所有请求都需要认证
                        .anyRequest().authenticated())

                // 注册JWT认证过滤器（在UsernamePasswordAuthenticationFilter之前执行）
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 配置异常处理：认证失败返回401而不是403
                .exceptionHandling(exception -> exception
                        // 未认证的处理（返回401）
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("认证失败，返回401，URI: {}, 异常: {}",
                                    request.getRequestURI(), authException.getMessage());
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"code\":401,\"message\":\"未登录或Token已过期\"}");
                        })
                        // 权限不足的处理（返回403）
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.warn("权限不足，返回403，URI: {}, 异常: {}",
                                    request.getRequestURI(), accessDeniedException.getMessage());
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"code\":403,\"message\":\"权限不足，无法访问此资源\"}");
                        }));

        log.info("Spring Security SecurityFilterChain 配置完成，JWT认证过滤器已注册");
        return http.build();
    }

    /**
     * 配置CORS跨域资源共享
     * <p>
     * 重要：暴露 X-New-Token 响应头，使前端能够接收到自动续期的新token
     * 
     * @return CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("配置 CORS 跨域资源共享");

        CorsConfiguration configuration = new CorsConfiguration();

        // 允许的源（开发环境允许所有源，生产环境应指定具体域名）
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));

        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 允许携带认证信息（如 Cookie）
        configuration.setAllowCredentials(true);

        // 【关键】暴露自定义响应头，使浏览器能够读取这些响应头
        // X-New-Token: 自动续期时后端返回的新token
        // X-Token-Expires-At: 新token的过期时间
        configuration.setExposedHeaders(Arrays.asList("X-New-Token", "X-Token-Expires-At"));

        // 预检请求的缓存时间（秒）
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("CORS 配置完成，已暴露响应头: X-New-Token, X-Token-Expires-At");
        return source;
    }
}
