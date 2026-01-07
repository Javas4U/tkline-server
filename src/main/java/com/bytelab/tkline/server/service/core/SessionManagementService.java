package com.bytelab.tkline.server.service.core;

/**
 * 会话管理服务接口
 * <p>
 * 提供用户会话管理功能，包括强制退出、会话失效等
 * 
 * @author Apex Tunnel Team
 * @since 2025-10-28
 */
public interface SessionManagementService {
    
    /**
     * 调度用户会话失效
     * <p>
     * 在指定延迟后强制用户退出所有会话
     * 
     * @param username 用户名
     * @param delayMinutes 延迟时间（分钟）
     */
    void scheduleSessionInvalidation(String username, int delayMinutes);
    
    /**
     * 立即强制用户退出所有会话
     * <p>
     * 清除用户的所有Token缓存
     * 
     * @param username 用户名
     */
    void invalidateAllSessions(String username);
    
    /**
     * 检查用户是否有活动会话
     * 
     * @param username 用户名
     * @return true如果有活动会话
     */
    boolean hasActiveSessions(String username);
}

