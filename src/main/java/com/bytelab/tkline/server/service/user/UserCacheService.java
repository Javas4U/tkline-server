package com.bytelab.tkline.server.service.user;

import com.bytelab.tkline.server.dto.user.UserInfoDTO;
import com.bytelab.tkline.server.service.core.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户信息缓存服务
 * <p>
 * 提供全局的用户信息缓存，避免频繁查询数据库
 * 使用ConcurrentHashMap实现内存缓存，后期可扩展为Redis
 * <p>
 * 使用场景：
 * 1. 用户登录后缓存用户信息
 * 2. 各模块需要用户信息时优先从缓存获取
 * 3. 用户信息更新时刷新缓存
 * 4. 用户退出登录时清除缓存
 * <p>
 * 创建日期：2025-10-23
 *
 * @author apex-tunnel
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCacheService {

    private final UserService userService;
    
    /**
     * 用户信息缓存
     * Key: userId
     * Value: UserInfoDTO
     */
    private final Map<Long, UserInfoDTO> userCache = new ConcurrentHashMap<>();
    
    /**
     * 获取用户信息（优先从缓存）
     * <p>
     * 1. 先从缓存获取
     * 2. 缓存不存在则从数据库查询
     * 3. 查询到后放入缓存
     *
     * @param userId 用户ID
     * @return 用户信息，不存在返回null
     */
    public UserInfoDTO getUserInfo(Long userId) {
        if (userId == null) {
            return null;
        }
        
        // 1. 先从缓存获取
        UserInfoDTO cachedUser = userCache.get(userId);
        if (cachedUser != null) {
            log.debug("从缓存获取用户信息，userId: {}", userId);
            return cachedUser;
        }
        
        // 2. 缓存不存在，从数据库查询
        log.debug("缓存未命中，从数据库查询用户信息，userId: {}", userId);
        UserInfoDTO userInfo = userService.getUserById(userId);
        
        // 3. 查询到后放入缓存
        if (userInfo != null) {
            putUserInfo(userId, userInfo);
        }
        
        return userInfo;
    }
    
    /**
     * 批量获取用户信息
     * <p>
     * 用于需要多个用户信息的场景，如订单列表显示用户名
     *
     * @param userIds 用户ID列表
     * @return 用户信息Map，Key为userId
     */
    public Map<Long, UserInfoDTO> getUserInfoBatch(Iterable<Long> userIds) {
        Map<Long, UserInfoDTO> result = new ConcurrentHashMap<>();
        
        for (Long userId : userIds) {
            if (userId != null) {
                UserInfoDTO userInfo = getUserInfo(userId);
                if (userInfo != null) {
                    result.put(userId, userInfo);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 放入缓存
     * <p>
     * 在以下场景调用：
     * 1. 用户登录成功后
     * 2. 从数据库查询用户后
     * 3. 用户信息更新后
     *
     * @param userId 用户ID
     * @param userInfo 用户信息
     */
    public void putUserInfo(Long userId, UserInfoDTO userInfo) {
        if (userId == null || userInfo == null) {
            return;
        }
        
        userCache.put(userId, userInfo);
        log.debug("缓存用户信息，userId: {}, username: {}", userId, userInfo.getUsername());
    }
    
    /**
     * 刷新缓存
     * <p>
     * 用户信息更新后调用，重新从数据库加载最新数据
     *
     * @param userId 用户ID
     */
    public void refreshUserInfo(Long userId) {
        if (userId == null) {
            return;
        }
        
        log.info("刷新用户缓存，userId: {}", userId);
        
        // 先清除旧缓存
        userCache.remove(userId);
        
        // 重新加载
        UserInfoDTO userInfo = userService.getUserById(userId);
        if (userInfo != null) {
            putUserInfo(userId, userInfo);
        }
    }
    
    /**
     * 清除缓存
     * <p>
     * 用户退出登录时调用
     *
     * @param userId 用户ID
     */
    public void removeUserInfo(Long userId) {
        if (userId == null) {
            return;
        }
        
        userCache.remove(userId);
        log.info("清除用户缓存，userId: {}", userId);
    }
    
    /**
     * 清空所有缓存
     * <p>
     * 谨慎使用，一般只用于：
     * 1. 系统维护
     * 2. 内存优化
     * 3. 测试环境
     */
    public void clearAll() {
        int size = userCache.size();
        userCache.clear();
        log.warn("清空所有用户缓存，共清除 {} 条记录", size);
    }
    
    /**
     * 获取缓存大小
     *
     * @return 当前缓存的用户数量
     */
    public int getCacheSize() {
        return userCache.size();
    }
    
    /**
     * 检查用户是否在缓存中
     *
     * @param userId 用户ID
     * @return true表示在缓存中
     */
    public boolean containsUser(Long userId) {
        return userId != null && userCache.containsKey(userId);
    }
}

