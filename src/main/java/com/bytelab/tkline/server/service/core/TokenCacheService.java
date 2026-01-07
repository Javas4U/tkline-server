package com.bytelab.tkline.server.service.core;

import com.bytelab.tkline.server.dto.auth.TokenInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Token缓存服务
 * <p>
 * 管理所有活跃的token，提供：
 * 1. Token有效性验证
 * 2. Token过期检查
 * 3. Token黑名单（强制下线）
 * 4. 单点登录控制
 * <p>
 * 设计原则：
 * - Token缓存和用户信息缓存分离
 * - Token只存储最小必要信息（userId、过期时间、角色）
 * - 用户详细信息在UserCacheService中，可跨token复用
 * <p>
 * 创建日期：2025-10-23
 *
 * @author apex-tunnel
 */
@Slf4j
@Service
public class TokenCacheService {
    
    /**
     * Token缓存
     * Key: token字符串
     * Value: TokenInfo（包含userId、过期时间、角色等）
     */
    private final Map<String, TokenInfo> tokenCache = new ConcurrentHashMap<>();
    
    /**
     * Token黑名单（已失效的token）
     * 用于主动退出登录、强制下线等场景
     */
    private final Set<String> tokenBlacklist = ConcurrentHashMap.newKeySet();
    
    /**
     * 用户ID到Token的映射（用于单点登录控制）
     * Key: userId
     * Value: Set<token>（同一用户可能有多个设备登录）
     */
    private final Map<Long, Set<String>> userTokens = new ConcurrentHashMap<>();
    
    /**
     * 用户名到Token的映射（用于按用户名清除会话）
     * Key: username
     * Value: Set<token>
     */
    private final Map<String, Set<String>> usernameTokens = new ConcurrentHashMap<>();
    
    /**
     * 保存Token
     *
     * @param token token字符串
     * @param tokenInfo token信息
     */
    public void putToken(String token, TokenInfo tokenInfo) {
        if (token == null || tokenInfo == null) {
            return;
        }
        
        // 1. 保存token信息
        tokenCache.put(token, tokenInfo);
        
        // 3. 维护username -> tokens映射
        if (tokenInfo.getUsername() != null) {
            usernameTokens.computeIfAbsent(tokenInfo.getUsername(), k -> ConcurrentHashMap.newKeySet())
                          .add(token);
        }

        log.info("缓存Token，userId: {}, username: {}, 过期时间: {}",
            tokenInfo.getUserId(), tokenInfo.getUsername(), tokenInfo.getExpireTime());
    }
    
    /**
     * 获取Token信息
     *
     * @param token token字符串
     * @return TokenInfo，不存在或已过期返回null
     */
    public TokenInfo getToken(String token) {
        if (token == null) {
            return null;
        }
        
        // 1. 检查黑名单
        if (tokenBlacklist.contains(token)) {
            log.debug("Token在黑名单中，token: {}", maskToken(token));
            return null;
        }
        
        // 2. 从缓存获取
        TokenInfo tokenInfo = tokenCache.get(token);
        if (tokenInfo == null) {
            log.debug("Token不存在缓存中，token: {}", maskToken(token));
            return null;
        }
        
        // 3. 检查是否过期
        if (tokenInfo.isExpired()) {
            log.info("Token已过期，userId: {}", tokenInfo.getUserId());
            removeToken(token);
            return null;
        }
        
        return tokenInfo;
    }
    
    /**
     * 验证Token是否有效
     *
     * @param token token字符串
     * @return true表示有效
     */
    public boolean isTokenValid(String token) {
        return getToken(token) != null;
    }
    
    /**
     * 移除Token（退出登录）
     *
     * @param token token字符串
     */
    public void removeToken(String token) {
        if (token == null) {
            return;
        }
        
        TokenInfo tokenInfo = tokenCache.remove(token);
        if (tokenInfo != null) {
            // 从userId -> tokens映射中移除
            Set<String> tokens = userTokens.get(tokenInfo.getUserId());
            if (tokens != null) {
                tokens.remove(token);
                if (tokens.isEmpty()) {
                    userTokens.remove(tokenInfo.getUserId());
                }
            }
            
            // 从username -> tokens映射中移除
            if (tokenInfo.getUsername() != null) {
                Set<String> userTokenSet = usernameTokens.get(tokenInfo.getUsername());
                if (userTokenSet != null) {
                    userTokenSet.remove(token);
                    if (userTokenSet.isEmpty()) {
                        usernameTokens.remove(tokenInfo.getUsername());
                    }
                }
            }
            
            log.info("移除Token，userId: {}, username: {}", tokenInfo.getUserId(), tokenInfo.getUsername());
        }
    }
    
    /**
     * 将Token加入黑名单（强制失效）
     * <p>
     * 用于：
     * 1. 用户主动退出登录
     * 2. 管理员强制下线
     * 3. 检测到异常行为
     *
     * @param token token字符串
     */
    public void addToBlacklist(String token) {
        if (token == null) {
            return;
        }
        
        tokenBlacklist.add(token);
        removeToken(token);
        
        log.warn("Token加入黑名单，token: {}", maskToken(token));
    }
    
    /**
     * 强制用户下线（移除该用户的所有token）
     * <p>
     * 用于：
     * 1. 修改密码后强制重新登录
     * 2. 账号被禁用
     * 3. 管理员强制下线
     *
     * @param userId 用户ID
     */
    public void kickoutUser(Long userId) {
        if (userId == null) {
            return;
        }
        
        Set<String> tokens = userTokens.remove(userId);
        if (tokens != null && !tokens.isEmpty()) {
            for (String token : tokens) {
                tokenCache.remove(token);
                tokenBlacklist.add(token);
            }
            
            log.warn("强制用户下线，userId: {}, 移除token数: {}", userId, tokens.size());
        }
    }
    
    /**
     * 获取用户的所有活跃token数量
     * <p>
     * 用于：
     * 1. 单点登录控制
     * 2. 设备数量限制
     *
     * @param userId 用户ID
     * @return token数量
     */
    public int getUserTokenCount(Long userId) {
        Set<String> tokens = userTokens.get(userId);
        return tokens != null ? tokens.size() : 0;
    }
    
    /**
     * 按用户名清除所有Token（强制用户下线）
     * <p>
     * 用于：
     * 1. 用户被加入黑名单
     * 2. 密码修改后强制重新登录
     * 3. 管理员强制下线
     *
     * @param username 用户名
     */
    public void clearUserTokens(String username) {
        if (username == null) {
            return;
        }
        
        Set<String> tokens = usernameTokens.remove(username);
        if (tokens != null && !tokens.isEmpty()) {
            for (String token : tokens) {
                TokenInfo tokenInfo = tokenCache.remove(token);
                if (tokenInfo != null) {
                    // 从userId映射中移除
                    Set<String> userTokenSet = userTokens.get(tokenInfo.getUserId());
                    if (userTokenSet != null) {
                        userTokenSet.remove(token);
                        if (userTokenSet.isEmpty()) {
                            userTokens.remove(tokenInfo.getUserId());
                        }
                    }
                }
                tokenBlacklist.add(token);
            }
            
            log.warn("按用户名强制下线，username: {}, 移除token数: {}", username, tokens.size());
        }
    }
    
    /**
     * 检查用户是否有活动会话
     *
     * @param username 用户名
     * @return true如果有活动会话
     */
    public boolean hasActiveTokens(String username) {
        Set<String> tokens = usernameTokens.get(username);
        return tokens != null && !tokens.isEmpty();
    }
    
    /**
     * 清理过期的token（定时任务调用）
     * <p>
     * 建议：每小时执行一次
     */
    public void cleanExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        
        Set<String> expiredTokens = tokenCache.entrySet().stream()
            .filter(entry -> entry.getValue().getExpireTime().isBefore(now))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        
        for (String token : expiredTokens) {
            removeToken(token);
        }
        
        log.info("清理过期Token，共清理 {} 个", expiredTokens.size());
    }
    
    /**
     * 清理黑名单中的过期token（定时任务调用）
     * <p>
     * 黑名单中的token在过期后可以安全清除
     */
    public void cleanBlacklist() {
        // 简单实现：直接清空黑名单
        // 生产环境可以检查每个token的过期时间
        int size = tokenBlacklist.size();
        tokenBlacklist.clear();
        log.info("清理Token黑名单，共清理 {} 个", size);
    }
    
    /**
     * 获取缓存统计信息
     */
    public TokenCacheStats getStats() {
        return new TokenCacheStats(
            tokenCache.size(),
            tokenBlacklist.size(),
            userTokens.size()
        );
    }
    
    /**
     * 掩码Token（用于日志，避免泄露）
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
    
    /**
     * Token缓存统计信息
     */
    public record TokenCacheStats(
        int activeTokens,      // 活跃token数
        int blacklistedTokens, // 黑名单token数
        int onlineUsers        // 在线用户数
    ) {}
}

