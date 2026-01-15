package com.bytelab.tkline.server.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytelab.tkline.server.entity.SysUser;
import com.bytelab.tkline.server.entity.UserLoginLog;
import com.bytelab.tkline.server.mapper.UserLoginLogMapper;
import com.bytelab.tkline.server.mapper.UserMapper;
import com.bytelab.tkline.server.service.user.LoginLogService;
import com.bytelab.tkline.server.util.HttpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl extends ServiceImpl<UserLoginLogMapper, UserLoginLog> implements LoginLogService {

    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long recordLoginSuccess(Long userId, String username, String token, Long tokenExpireTime, HttpServletRequest request) {
        UserLoginLog loginLog = new UserLoginLog();
        
        // 基本信息
        loginLog.setUserId(userId);
        loginLog.setUsername(username);
        loginLog.setLoginStatus(1);  // 成功
        loginLog.setLoginType("PASSWORD");
        
        // 解析请求信息
        loginLog.setLoginIp(HttpUtil.getClientIp(request));
        loginLog.setBrowser(HttpUtil.getBrowser(request));
        loginLog.setOs(HttpUtil.getOperatingSystem(request));
        loginLog.setDeviceType(HttpUtil.getDeviceType(request));
        loginLog.setDeviceId(HttpUtil.generateDeviceId(request));
        
        // Token信息
        loginLog.setToken(token);
        loginLog.setTokenExpireTime(LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(tokenExpireTime),
                java.time.ZoneId.systemDefault()
        ));

        this.save(loginLog);
        
        log.info("记录登录成功日志，userId: {}, ip: {}, device: {}", 
                userId, loginLog.getLoginIp(), loginLog.getDeviceType());
        
        return loginLog.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long recordLoginFailure(String username, String failReason, HttpServletRequest request) {
        UserLoginLog loginLog = new UserLoginLog();

        // 根据用户名查询用户ID
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
        );

        // 基本信息
        // 如果用户存在则设置真实ID，否则设置0表示用户不存在
        loginLog.setUserId(user != null ? user.getId() : 0L);
        loginLog.setUsername(username);
        loginLog.setLoginStatus(0);  // 失败
        loginLog.setLoginType("PASSWORD");
        loginLog.setFailReason(failReason);

        // 解析请求信息
        loginLog.setLoginIp(HttpUtil.getClientIp(request));
        loginLog.setBrowser(HttpUtil.getBrowser(request));
        loginLog.setOs(HttpUtil.getOperatingSystem(request));
        loginLog.setDeviceType(HttpUtil.getDeviceType(request));
        loginLog.setDeviceId(HttpUtil.generateDeviceId(request));

        this.save(loginLog);

        log.warn("记录登录失败日志，username: {}, userId: {}, ip: {}, reason: {}",
                username, user != null ? user.getId() : "未知", loginLog.getLoginIp(), failReason);

        return loginLog.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordLogout(String token) {
        LambdaUpdateWrapper<UserLoginLog> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserLoginLog::getToken, token)
                .set(UserLoginLog::getLogoutTime, LocalDateTime.now());

        // 计算在线时长
        UserLoginLog loginLog = this.getOne(
                new LambdaQueryWrapper<UserLoginLog>()
                        .eq(UserLoginLog::getToken, token)
        );
        
        if (loginLog != null && loginLog.getCreateTime() != null) {
            long duration = Duration.between(loginLog.getCreateTime(), LocalDateTime.now()).getSeconds();
            updateWrapper.set(UserLoginLog::getOnlineDuration, (int) duration);
            
            log.info("记录登出日志，userId: {}, onlineDuration: {}秒", 
                    loginLog.getUserId(), duration);
        }

        this.update(null, updateWrapper);
    }

    @Override
    public List<UserLoginLog> getUserLoginHistory(Long userId, Integer page, Integer size) {
        Page<UserLoginLog> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<UserLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLoginLog::getUserId, userId)
                .orderByDesc(UserLoginLog::getCreateTime);

        IPage<UserLoginLog> result = this.page(pageParam, wrapper);
        return result.getRecords();
    }

    @Override
    public List<UserLoginLog> getOnlineDevices(Long userId) {
        LambdaQueryWrapper<UserLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLoginLog::getUserId, userId)
                .eq(UserLoginLog::getLoginStatus, 1)
                .isNull(UserLoginLog::getLogoutTime)
                .gt(UserLoginLog::getTokenExpireTime, LocalDateTime.now())
                .orderByDesc(UserLoginLog::getCreateTime);

        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean forceLogout(Long loginLogId, Long userId) {
        // 验证权限：只能下线自己的设备
        UserLoginLog loginLog = this.getById(loginLogId);
        if (loginLog == null || !loginLog.getUserId().equals(userId)) {
            log.warn("强制下线失败：无权限，loginLogId: {}, userId: {}", loginLogId, userId);
            return false;
        }
        
        LambdaUpdateWrapper<UserLoginLog> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserLoginLog::getId, loginLogId)
                .set(UserLoginLog::getLogoutTime, LocalDateTime.now());
        
        // 计算在线时长
        if (loginLog.getCreateTime() != null) {
            long duration = Duration.between(loginLog.getCreateTime(), LocalDateTime.now()).getSeconds();
            updateWrapper.set(UserLoginLog::getOnlineDuration, (int) duration);
        }

        this.update(null, updateWrapper);
        
        log.info("强制下线设备，userId: {}, loginLogId: {}, device: {}", 
                userId, loginLogId, loginLog.getDeviceType());
        
        return true;
    }

    @Override
    public List<UserLoginLog> getRecentFailures(String username, int count) {
        LambdaQueryWrapper<UserLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLoginLog::getUsername, username)
                .eq(UserLoginLog::getLoginStatus, 0)  // 失败
                .orderByDesc(UserLoginLog::getCreateTime)
                .last("LIMIT " + count);

        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanExpiredLogs(int daysToKeep) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysToKeep);
        
        LambdaQueryWrapper<UserLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(UserLoginLog::getCreateTime, cutoffTime);

        int deletedCount = baseMapper.delete(wrapper);
        
        log.info("清理过期登录日志，保留天数: {}, 清理数量: {}", daysToKeep, deletedCount);
        
        return deletedCount;
    }
}

