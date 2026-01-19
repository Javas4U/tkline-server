package com.bytelab.tkline.server.util;

import com.bytelab.tkline.server.dto.user.UserInfoDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Spring Security 工具类
 * <p>
 * 统一从 SecurityContext 获取当前登录用户信息
 * <p>
 * 使用场景：
 * 1. Service 层获取当前登录用户
 * 2. 记录操作日志时获取操作人
 * 3. 数据权限校验
 * 4. MyBatis-Plus 审计字段自动填充
 * <p>
 * 优势：
 * - 与 Spring Security 完全集成
 * - 自动生命周期管理（无需手动清理）
 * - 统一的用户信息获取方式
 * - 避免重复的 ThreadLocal 管理
 * <p>
 * 创建日期：2025-11-03
 *
 * @author Apex Tunnel Team
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户的完整信息
     * <p>
     * 从 Spring Security 的 SecurityContext 中获取
     * Authentication.principal 存储的是 UserInfoDTO 对象
     *
     * @return 用户信息 DTO，未登录返回 null
     */
    public static UserInfoDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        
        // principal 应该是 UserInfoDTO 类型
        if (principal instanceof UserInfoDTO) {
            return (UserInfoDTO) principal;
        }
        
        // 如果不是预期类型，返回 null
        // 这种情况通常不应该发生，除非是匿名用户或特殊情况
        return null;
    }

    /**
     * 获取当前登录用户的 ID
     *
     * @return 用户 ID，未登录返回 null
     */
    public static Long getCurrentUserId() {
        UserInfoDTO user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前登录用户的用户名
     *
     * @return 用户名，未登录返回 null
     */
    public static String getCurrentUsername() {
        UserInfoDTO user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 获取当前登录用户的角色
     *
     * @return 用户角色（USER/ADMIN/SUPER_ADMIN），未登录返回 null
     */
    public static String getCurrentUserRole() {
        UserInfoDTO user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }

    /**
     * 获取当前登录用户的邮箱
     *
     * @return 邮箱，未登录返回 null
     */
    public static String getCurrentUserEmail() {
        UserInfoDTO user = getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    /**
     * 检查当前用户是否已登录
     *
     * @return true 表示已登录，false 表示未登录
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null 
                && authentication.isAuthenticated() 
                && authentication.getPrincipal() instanceof UserInfoDTO;
    }

    /**
     * 检查当前用户是否是管理员
     *
     * @return true 表示是管理员（ADMIN 或 SUPER_ADMIN）
     */
    public static boolean isAdmin() {
        String role = getCurrentUserRole();
        return "ADMIN".equals(role) || "SUPER_ADMIN".equals(role);
    }

    /**
     * 检查当前用户是否是超级管理员
     *
     * @return true 表示是超级管理员
     */
    public static boolean isSuperAdmin() {
        String role = getCurrentUserRole();
        return "SUPER_ADMIN".equals(role);
    }

    /**
     * 获取当前 Authentication 对象
     * <p>
     * 高级用法：需要直接操作 Authentication 时使用
     *
     * @return Authentication 对象，未认证返回 null
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}

