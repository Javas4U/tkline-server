package com.bytelab.tkline.server.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bytelab.tkline.server.entity.UserLoginLog;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 登录日志服务接口
 */
public interface LoginLogService extends IService<UserLoginLog> {
    
    /**
     * 记录登录成功日志
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param token    JWT Token
     * @param tokenExpireTime Token过期时间（毫秒时间戳）
     * @param request  HTTP请求对象
     * @return 登录日志ID
     */
    Long recordLoginSuccess(Long userId, String username, String token, Long tokenExpireTime, HttpServletRequest request);
    
    /**
     * 记录登录失败日志
     *
     * @param username    用户名
     * @param failReason  失败原因
     * @param request     HTTP请求对象
     * @return 登录日志ID
     */
    Long recordLoginFailure(String username, String failReason, HttpServletRequest request);
    
    /**
     * 记录登出日志
     *
     * @param token 退出的Token
     */
    void recordLogout(String token);
    
    /**
     * 获取用户登录历史
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页大小
     * @return 登录日志列表
     */
    List<UserLoginLog> getUserLoginHistory(Long userId, Integer page, Integer size);
    
    /**
     * 获取用户在线设备列表
     *
     * @param userId 用户ID
     * @return 在线设备的登录日志列表
     */
    List<UserLoginLog> getOnlineDevices(Long userId);
    
    /**
     * 强制下线指定设备
     *
     * @param loginLogId 登录日志ID
     * @param userId     用户ID（用于权限验证）
     * @return 是否成功
     */
    boolean forceLogout(Long loginLogId, Long userId);
    
    /**
     * 获取用户最近N次登录失败记录
     *
     * @param username 用户名
     * @param count    记录数量
     * @return 失败记录列表
     */
    List<UserLoginLog> getRecentFailures(String username, int count);
    
    /**
     * 清理过期的登录日志（定时任务使用）
     *
     * @param daysToKeep 保留天数
     * @return 清理的记录数
     */
    int cleanExpiredLogs(int daysToKeep);
}

