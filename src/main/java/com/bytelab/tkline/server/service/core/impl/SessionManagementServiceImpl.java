package com.bytelab.tkline.server.service.core.impl;

import com.bytelab.tkline.server.service.core.SessionManagementService;
import com.bytelab.tkline.server.service.core.TokenCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * 会话管理服务实现类
 * <p>
 * 使用TaskScheduler实现延迟会话失效功能
 * 
 * @author Apex Tunnel Team
 * @since 2025-10-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionManagementServiceImpl implements SessionManagementService {
    
    private final TaskScheduler taskScheduler;
    private final TokenCacheService tokenCacheService;
    
    @Override
    public void scheduleSessionInvalidation(String username, int delayMinutes) {
        log.info("调度用户会话失效，username: {}, 延迟: {}分钟", username, delayMinutes);
        
        // 计算执行时间
        Instant executionTime = Instant.now().plusSeconds(delayMinutes * 60L);
        
        // 调度任务
        taskScheduler.schedule(() -> {
            try {
                invalidateAllSessions(username);
                log.info("用户会话已失效（调度任务），username: {}", username);
            } catch (Exception e) {
                log.error("强制用户退出失败，username: {}, 原因: {}", username, e.getMessage(), e);
            }
        }, executionTime);
        
        log.info("会话失效任务已调度，username: {}, 执行时间: {}", username, executionTime);
    }
    
    @Override
    public void invalidateAllSessions(String username) {
        log.info("立即强制用户退出所有会话，username: {}", username);
        
        // 清除用户的Token缓存
        tokenCacheService.clearUserTokens(username);
        
        log.info("用户所有会话已清除，username: {}", username);
    }
    
    @Override
    public boolean hasActiveSessions(String username) {
        // 检查TokenCache中是否有该用户的Token
        return tokenCacheService.hasActiveTokens(username);
    }
}

