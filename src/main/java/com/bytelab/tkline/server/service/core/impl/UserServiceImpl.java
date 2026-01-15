package com.bytelab.tkline.server.service.core.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytelab.tkline.server.converter.UserConverter;
import com.bytelab.tkline.server.dto.auth.TokenInfo;
import com.bytelab.tkline.server.dto.user.*;
import com.bytelab.tkline.server.entity.SysUser;
import com.bytelab.tkline.server.entity.UserLoginLog;
import com.bytelab.tkline.server.enums.ResetPasswordErrorType;
import com.bytelab.tkline.server.enums.UserRole;
import com.bytelab.tkline.server.exception.BusinessException;
import com.bytelab.tkline.server.mapper.UserMapper;
import com.bytelab.tkline.server.service.modules.EmailService;
import com.bytelab.tkline.server.service.user.LoginLogService;
import com.bytelab.tkline.server.service.user.UserCacheService;
import com.bytelab.tkline.server.service.core.UserService;
import com.bytelab.tkline.server.service.core.TokenCacheService;
import com.bytelab.tkline.server.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 集成 RSA 密钥解密和 BCrypt 密码加密
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, SysUser> implements UserService {

    private final UserConverter userConverter;
    private final JwtUtil jwtUtil;
    private final LoginLogService loginLogService;
    private final EmailService emailService;
    private final UserCacheService userCacheService; // ⭐ 用户信息缓存（@Lazy避免循环依赖）
    private final TokenCacheService tokenCacheService; // ⭐ Token缓存

    /**
     * 构造函数注入（使用@Lazy避免循环依赖）
     * <p>
     * 循环依赖链路：UserCacheService -> UserServiceImpl -> UserCacheService
     */
    public UserServiceImpl(
            UserConverter userConverter,
            JwtUtil jwtUtil,
            LoginLogService loginLogService,
            EmailService emailService,
            @Lazy UserCacheService userCacheService,
            TokenCacheService tokenCacheService) {
        this.userConverter = userConverter;
        this.jwtUtil = jwtUtil;
        this.loginLogService = loginLogService;
        this.emailService = emailService;
        this.userCacheService = userCacheService;
        this.tokenCacheService = tokenCacheService;
    }

    /**
     * 登录失败最大尝试次数（5分钟内）
     */
    private static final int MAX_LOGIN_ATTEMPTS = 5;

    /**
     * 锁定时间（分钟）
     */
    private static final int LOCK_DURATION_MINUTES = 15;

    @Override
    public List<UserInfoDTO> getUserList(Integer page, Integer size) {
        Page<SysUser> pageParam = new Page<>(page, size);
        IPage<SysUser> userPage = this.page(pageParam, null);

        return userPage.getRecords().stream()
                .map(userConverter::toUserInfoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserInfoDTO getUserById(Long id) {
        SysUser sysUser = this.getById(id);
        if (sysUser == null) {
            throw new BusinessException("用户不存在，ID: " + id);
        }
        return userConverter.toUserInfoDTO(sysUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoDTO createUser(CreateUserRequest request) {
        log.info("开始创建用户，username: {}", request.getUsername());

        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername());
        Long count = this.count(wrapper);
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 创建用户实体
        // 注意：request中的加密字段已被DecryptRequestBodyAdvice自动解密为明文
        SysUser sysUser = new SysUser();
        sysUser.setUsername(request.getUsername());
        sysUser.setPassword(request.getPassword()); // 已是明文，BCryptTypeHandler自动散列
        sysUser.setEmail(request.getEmail()); // 已是明文
        sysUser.setPhone(request.getPhone()); // 已是明文
        sysUser.setStatus(1); // 默认启用

        // 3. 保存到数据库
        this.save(sysUser);

        log.info("用户创建成功，ID: {}, username: {}", sysUser.getId(), sysUser.getUsername());

        return userConverter.toUserInfoDTO(sysUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoDTO updateUser(UpdateUserRequest request) {
        log.info("开始更新用户，ID: {}", request.getId());

        // 1. 查询用户
        SysUser sysUser = this.getById(request.getId());
        if (sysUser == null) {
            throw new BusinessException("用户不存在，ID: " + request.getId());
        }

        // 2. 更新敏感字段（已被DecryptRequestBodyAdvice自动解密）
        if (StringUtils.hasText(request.getEmail())) {
            sysUser.setEmail(request.getEmail());
        }

        if (StringUtils.hasText(request.getPhone())) {
            sysUser.setPhone(request.getPhone());
        }

        if (request.getStatus() != null) {
            sysUser.setStatus(request.getStatus());
        }

        // 4. 更新数据库
        this.updateById(sysUser);

        log.info("用户更新成功，ID: {}", sysUser.getId());

        return userConverter.toUserInfoDTO(sysUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        log.info("开始删除用户，ID: {}", id);

        SysUser sysUser = this.getById(id);
        if (sysUser == null) {
            throw new BusinessException("用户不存在，ID: " + id);
        }

        // 逻辑删除
        int deleted = baseMapper.deleteById(id);

        log.info("用户删除成功，ID: {}", id);

        return deleted > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(ChangePasswordRequest request) {
        log.info("开始修改密码，userID: {}", request.getUserId());

        // 1. 查询用户
        SysUser sysUser = this.getById(request.getUserId());
        if (sysUser == null) {
            throw new BusinessException("用户不存在，ID: " + request.getUserId());
        }

        // 2. 验证旧密码（已被DecryptRequestBodyAdvice自动解密）
        if (!com.bytelab.tkline.server.handler.BCryptTypeHandler.verify(
                request.getOldPassword(), // 已自动解密的明文
                sysUser.getPassword())) { // 数据库散列值
            log.warn("旧密码验证失败，userID: {}", request.getUserId());
            throw new BusinessException("旧密码不正确");
        }

        // 3. 密码强度检查
        validatePasswordStrength(request.getNewPassword());

        // 4. 设置新密码（已是明文，BCryptTypeHandler自动散列）
        sysUser.setPassword(request.getNewPassword());

        // 5. 更新数据库
        this.updateById(sysUser);

        log.info("密码修改成功，userID: {}", request.getUserId());

        return true;
    }

    @Override
    public UserInfoDTO getUserByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);

        SysUser sysUser = this.getOne(wrapper);
        if (sysUser == null) {
            throw new BusinessException("用户不存在，username: " + username);
        }

        return userConverter.toUserInfoDTO(sysUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录请求，username: {}", request.getUsername());

        // 获取HttpServletRequest
        HttpServletRequest httpRequest = getCurrentHttpRequest();

        // 1. 检查登录失败次数限制
        checkLoginAttempts(request.getUsername());

        // 2. 查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername());
        SysUser sysUser = this.getOne(wrapper);

        if (sysUser == null) {
            log.warn("登录失败：用户不存在，username: {}", request.getUsername());
            loginLogService.recordLoginFailure(request.getUsername(), "用户不存在", httpRequest);
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 检查用户状态
        if (sysUser.getStatus() == null || sysUser.getStatus() == 0) {
            log.warn("登录失败：用户已禁用，username: {}", request.getUsername());
            loginLogService.recordLoginFailure(request.getUsername(), "用户已被禁用", httpRequest);
            throw new BusinessException("用户已被禁用");
        }

        // 4. 验证密码
        // 注意：request.password 已被DecryptRequestBodyAdvice自动解密为明文
        boolean passwordMatch = com.bytelab.tkline.server.handler.BCryptTypeHandler.verify(
                request.getPassword(), // 已自动解密的明文
                sysUser.getPassword() // 数据库读取的散列值
        );
        log.debug("密码验证结果，username: {}, match: {}", request.getUsername(), passwordMatch);

        if (!passwordMatch) {
            log.warn("登录失败：密码错误，username: {}", request.getUsername());
            loginLogService.recordLoginFailure(request.getUsername(), "密码错误", httpRequest);
            throw new BusinessException("用户名或密码错误");
        }

        // 6. 转换用户信息
        UserInfoDTO userInfo = userConverter.toUserInfoDTO(sysUser);

        // 7. ⭐⭐⭐ 双层缓存集成（关键！）

        // 7.1 第二层：缓存用户信息（详细信息，长期保留）
        userCacheService.putUserInfo(sysUser.getId(), userInfo);
        log.debug("缓存用户信息，userId: {}, username: {}", sysUser.getId(), sysUser.getUsername());

        // 7.2 生成JWT Token
        String token = jwtUtil.generateToken(sysUser.getId(), sysUser.getUsername());

        // 7.3 计算过期时间（30分钟后）
        long expiresAt = System.currentTimeMillis() + 30 * 60 * 1000;
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(30);

        // 7.4 第一层：缓存TokenInfo（轻量级，快速验证）
        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setUserId(sysUser.getId());
        tokenInfo.setExpireTime(expireTime);
        tokenInfo.setRoles(getUserRoles(sysUser));
        tokenInfo.setCreateTime(LocalDateTime.now());
        tokenInfo.setIpAddress(getClientIp(httpRequest));

        tokenCacheService.putToken(token, tokenInfo);
        log.info("缓存Token信息，userId: {}, 过期时间: {}", sysUser.getId(), expireTime);

        // 8. 记录登录成功日志
        loginLogService.recordLoginSuccess(
                sysUser.getId(),
                sysUser.getUsername(),
                token,
                expiresAt,
                httpRequest);

        log.info("登录成功（双层缓存已生效），username: {}, userId: {}, tokenExpiresAt: {}",
                request.getUsername(), sysUser.getId(), new java.util.Date(expiresAt));

        // 9. 构建响应
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresAt(expiresAt)
                .userInfo(userInfo)
                .build();

    }

    /**
     * 检查登录失败次数限制
     */
    private void checkLoginAttempts(String username) {
        List<UserLoginLog> recentFailures = loginLogService.getRecentFailures(username, MAX_LOGIN_ATTEMPTS);

        if (recentFailures.isEmpty()) {
            return;
        }

        // 检查5分钟内的失败次数
        java.time.LocalDateTime fiveMinutesAgo = java.time.LocalDateTime.now().minusMinutes(5);
        long recentFailCount = recentFailures.stream()
                .filter(log -> log.getCreateTime() != null && log.getCreateTime().isAfter(fiveMinutesAgo))
                .count();

        if (recentFailCount >= MAX_LOGIN_ATTEMPTS) {
            // 检查最后一次失败是否在锁定时间内
            UserLoginLog lastFailure = recentFailures.get(0);
            java.time.LocalDateTime lockUntil = lastFailure.getCreateTime().plusMinutes(LOCK_DURATION_MINUTES);

            if (java.time.LocalDateTime.now().isBefore(lockUntil)) {
                long minutesRemaining = java.time.Duration.between(
                        java.time.LocalDateTime.now(),
                        lockUntil).toMinutes();

                log.warn("登录失败次数过多，账号已锁定，username: {}, 剩余时间: {}分钟",
                        username, minutesRemaining);

                throw new BusinessException(
                        String.format("登录失败次数过多，账号已锁定 %d 分钟", minutesRemaining + 1));
            }
        }
    }

    @Override
    public LoginResponse refreshToken(String oldToken) {
        log.info("Token刷新请求");

        // 1. 验证旧Token并提取用户信息
        Long userId = jwtUtil.getUserIdFromToken(oldToken);
        String username = jwtUtil.getUsernameFromToken(oldToken);

        // 2. 查询用户（验证用户是否仍然存在且未被禁用）
        SysUser sysUser = this.getById(userId);
        if (sysUser == null) {
            throw new BusinessException("用户不存在");
        }

        if (sysUser.getStatus() == null || sysUser.getStatus() == 0) {
            throw new BusinessException("用户已被禁用");
        }

        // 3. 生成新Token
        String newToken = jwtUtil.refreshToken(oldToken);

        // 4. 计算新的过期时间
        long expiresAt = System.currentTimeMillis() + 30 * 60 * 1000;

        // 5. 转换用户信息
        UserInfoDTO userInfo = userConverter.toUserInfoDTO(sysUser);

        log.info("Token刷新成功，userId: {}, username: {}", userId, username);

        // 6. 构建响应
        return LoginResponse.builder()
                .token(newToken)
                .tokenType("Bearer")
                .expiresAt(expiresAt)
                .userInfo(userInfo)
                .build();
    }

    /**
     * 获取当前HTTP请求对象
     */
    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            throw new BusinessException("无法获取HTTP请求上下文");
        }

        return attributes.getRequest();
    }

    /**
     * 验证密码强度
     *
     * @param password 密码
     */
    private void validatePasswordStrength(String password) {
        if (password.length() < 6) {
            throw new BusinessException("密码长度至少为6位");
        }

        // 可以添加更多密码强度检查规则
        // 例如：必须包含大小写字母、数字、特殊字符等
    }

    @Override
    public boolean checkUsernameExists(String username) {
        if (!StringUtils.hasText(username)) {
            return false;
        }

        // 直接使用SQL查询用户名是否存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        Long count = this.count(wrapper);

        boolean exists = count != null && count > 0;
        log.debug("用户名存在性检查，用户名：{}, 存在：{}", username, exists);
        return exists;
    }

    @Override
    public boolean checkEmailExists(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }

        // 直接使用SQL查询邮箱是否存在（性能更优）
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getEmail, email);
        Long count = this.count(wrapper);

        boolean exists = count != null && count > 0;
        log.debug("邮箱存在性检查，邮箱：{}, 存在：{}", email, exists);
        return exists;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse emailLogin(EmailLoginRequest request) {
        log.info("邮箱验证码登录请求，邮箱：{}", request.getEmail());

        // 获取HttpServletRequest
        HttpServletRequest httpRequest = getCurrentHttpRequest();

        // 1. 验证邮箱和验证码
        if (!StringUtils.hasText(request.getEmail()) || !StringUtils.hasText(request.getCode())) {
            throw new BusinessException("邮箱和验证码不能为空");
        }

        // 2. 验证验证码是否正确（LOGIN用途）
        boolean isCodeValid = emailService.verifyCode(
                request.getEmail(),
                request.getCode(),
                SendCodeRequest.CodePurpose.LOGIN);
        if (!isCodeValid) {
            // 记录失败日志
            loginLogService.recordLoginFailure(
                    request.getEmail(),
                    "验证码错误或已过期",
                    httpRequest);
            throw new BusinessException("验证码错误或已过期");
        }

        // 3. 根据邮箱查询用户（直接SQL查询）
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getEmail, request.getEmail());
        SysUser sysUser = this.getOne(wrapper);

        if (sysUser == null) {
            // 记录失败日志
            loginLogService.recordLoginFailure(
                    request.getEmail(),
                    "该邮箱未注册",
                    httpRequest);
            throw new BusinessException("该邮箱未注册");
        }

        // 4. 检查用户状态
        if (sysUser.getStatus() == 0) {
            loginLogService.recordLoginFailure(
                    sysUser.getUsername(),
                    "账户已禁用",
                    httpRequest);
            throw new BusinessException("账户已被禁用");
        }

        // 5. 生成JWT Token（30分钟有效期）
        String token = jwtUtil.generateToken(sysUser.getId(), sysUser.getUsername());
        long expiresAt = System.currentTimeMillis() + 30 * 60 * 1000;

        // 6. 清除验证码
        emailService.clearCode(request.getEmail(), SendCodeRequest.CodePurpose.LOGIN);

        // 7. 记录登录成功日志
        loginLogService.recordLoginSuccess(
                sysUser.getId(),
                sysUser.getUsername(),
                token,
                expiresAt,
                httpRequest);

        // 8. 转换用户信息
        UserInfoDTO userInfo = userConverter.toUserInfoDTO(sysUser);

        log.info("邮箱登录成功，用户：{}, 邮箱：{}", sysUser.getUsername(), request.getEmail());

        // 9. 构建响应
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresAt(expiresAt)
                .userInfo(userInfo)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(ResetPasswordRequest request) {
        log.info("重置密码请求，邮箱：{}", request.getEmail());

        // 1. 验证邮箱和验证码
        if (!StringUtils.hasText(request.getEmail()) || !StringUtils.hasText(request.getUsername())) {
            throw new BusinessException("邮箱和验证码不能为空");
        }

        // 2. 验证验证码是否正确（RESET_PASSWORD用途）
        boolean isCodeValid = emailService.verifyCode(
                request.getEmail(),
                request.getCode(),
                SendCodeRequest.CodePurpose.RESET_PASSWORD);
        if (!isCodeValid) {
            log.warn("验证码错误或已过期，邮箱：{}", request.getEmail());
            throw new BusinessException("验证码错误或已过期");
        }

        // 3. 根据邮箱查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getEmail, request.getEmail());
        SysUser sysUser = this.getOne(wrapper);

        if (sysUser == null) {
            log.warn("邮箱未注册，无法重置密码，邮箱：{}", request.getEmail());
            throw new BusinessException("该邮箱未注册");
        }

        // 4. 检查用户状态
        if (sysUser.getStatus() == 0) {
            log.warn("用户已禁用，无法重置密码，邮箱：{}", request.getEmail());
            throw new BusinessException("账户已被禁用");
        }

        // 5. 密码强度检查（新密码已被@Decrypt自动解密）
        validatePasswordStrength(request.getNewPassword());

        // 6. 设置新密码（@BCrypt自动散列）
        sysUser.setPassword(request.getNewPassword());

        // 7. 更新数据库
        this.updateById(sysUser);

        // 8. 清除验证码
        emailService.clearCode(request.getEmail(), SendCodeRequest.CodePurpose.RESET_PASSWORD);

        log.info("密码重置成功，用户：{}, 邮箱：{}", sysUser.getUsername(), request.getEmail());

        return true;
    }

    @Override
    public void sendResetPasswordLink(String email, String resetUrl) {
        log.info("发送重置密码链接，邮箱：{}", email);

        // 1. 验证邮箱是否已注册
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getEmail, email);
        SysUser sysUser = this.getOne(wrapper);

        if (sysUser == null) {
            throw new BusinessException("该邮箱未注册");
        }

        // 2. 检查用户状态
        if (sysUser.getStatus() == 0) {
            throw new BusinessException("账户已被禁用");
        }

        // 3. 调用EmailService发送重置链接
        String token = emailService.sendResetPasswordLink(email, sysUser.getUsername(), resetUrl);

        log.info("重置密码链接发送成功，用户：{}, 邮箱：{}, token: {}", sysUser.getUsername(), email, token);
    }

    @Override
    public ValidateResetTokenResponse validateResetToken(
            ValidateResetTokenRequest request) {
        log.info("验证重置Token，用户名：{}, 邮箱：{}", request.getUsername(), request.getEmail());

        // 1. 检查用户名是否存在
        LambdaQueryWrapper<SysUser> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(SysUser::getUsername, request.getUsername());
        SysUser sysUserByUsername = this.getOne(usernameWrapper);

        if (sysUserByUsername == null) {
            log.warn("用户名不存在：{}", request.getUsername());
            return ValidateResetTokenResponse.builder()
                    .valid(false)
                    .errorType(ResetPasswordErrorType.USER_NOT_FOUND.getCode())
                    .message(ResetPasswordErrorType.USER_NOT_FOUND.getMessage())
                    .build();
        }

        // 2. 检查邮箱是否存在
        LambdaQueryWrapper<SysUser> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(SysUser::getEmail, request.getEmail());
        SysUser sysUserByEmail = this.getOne(emailWrapper);

        if (sysUserByEmail == null) {
            log.warn("邮箱不存在：{}", request.getEmail());
            return ValidateResetTokenResponse.builder()
                    .valid(false)
                    .errorType(ResetPasswordErrorType.EMAIL_NOT_FOUND.getCode())
                    .message(ResetPasswordErrorType.EMAIL_NOT_FOUND.getMessage())
                    .build();
        }

        // 3. 验证用户名和邮箱是否属于同一个用户
        if (!sysUserByUsername.getId().equals(sysUserByEmail.getId())) {
            log.warn("邮箱与用户名不匹配，用户名：{}, 邮箱：{}", request.getUsername(), request.getEmail());
            return ValidateResetTokenResponse.builder()
                    .valid(false)
                    .errorType(ResetPasswordErrorType.EMAIL_NOT_MATCH.getCode())
                    .message(ResetPasswordErrorType.EMAIL_NOT_MATCH.getMessage())
                    .build();
        }

        // 4. 验证Token是否有效（包括过期检查和Token匹配检查）
        EmailService.ResetTokenInfo tokenInfo = emailService.verifyResetTokenWithDetails(
                request.getUsername(),
                request.getEmail(),
                request.getToken());

        if (tokenInfo == null) {
            // Token无效的可能原因：
            // - Token已过期
            // - Token不存在
            // - Token与用户信息不匹配

            // 尝试进一步判断具体原因
            boolean tokenExists = emailService.verifyResetToken(request.getEmail(), request.getToken());
            if (!tokenExists) {
                // 判断是否是过期还是不存在/不匹配
                log.warn("Token无效或已过期，用户名：{}, 邮箱：{}", request.getUsername(), request.getEmail());
                return ValidateResetTokenResponse.builder()
                        .valid(false)
                        .errorType(ResetPasswordErrorType.TOKEN_EXPIRED.getCode())
                        .message(ResetPasswordErrorType.TOKEN_EXPIRED.getMessage())
                        .build();
            } else {
                // Token存在但与用户信息不匹配
                log.warn("Token与用户信息不匹配，用户名：{}, 邮箱：{}", request.getUsername(), request.getEmail());
                return ValidateResetTokenResponse.builder()
                        .valid(false)
                        .errorType(ResetPasswordErrorType.TOKEN_NOT_MATCH.getCode())
                        .message(ResetPasswordErrorType.TOKEN_NOT_MATCH.getMessage())
                        .build();
            }
        }

        // 5. 所有验证通过
        log.info("重置Token验证成功，用户名：{}, 邮箱：{}", request.getUsername(), request.getEmail());

        return ValidateResetTokenResponse.builder()
                .valid(true)
                .username(request.getUsername())
                .email(request.getEmail())
                .message("验证成功，可以设置新密码")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPasswordByToken(ResetPasswordByTokenRequest request) {
        log.info("通过Token重置密码，用户名：{}, 邮箱：{}", request.getUsername(), request.getEmail());

        // 1. 验证参数
        if (!StringUtils.hasText(request.getUsername()) ||
                !StringUtils.hasText(request.getEmail()) ||
                !StringUtils.hasText(request.getToken())) {
            throw new BusinessException("用户名、邮箱和Token不能为空");
        }

        // 2. 验证Token（复用validateResetToken的逻辑）
        ValidateResetTokenRequest validateRequest = new ValidateResetTokenRequest();
        validateRequest.setUsername(request.getUsername());
        validateRequest.setEmail(request.getEmail());
        validateRequest.setToken(request.getToken());

        ValidateResetTokenResponse validateResponse = validateResetToken(validateRequest);

        // 检查验证结果，如果失败则抛出异常
        if (!validateResponse.getValid()) {
            log.warn("Token验证失败，错误类型：{}", validateResponse.getErrorType());
            throw new BusinessException(validateResponse.getMessage());
        }

        // 3. 查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getEmail, request.getEmail());
        SysUser sysUser = this.getOne(wrapper);

        if (sysUser == null) {
            log.warn("用户不存在，邮箱：{}", request.getEmail());
            throw new BusinessException("用户不存在");
        }

        // 4. 检查用户状态
        if (sysUser.getStatus() == 0) {
            log.warn("用户已禁用，无法重置密码，邮箱：{}", request.getEmail());
            throw new BusinessException("账户已被禁用");
        }

        // 5. 密码强度检查（新密码已被@Decrypt自动解密）
        validatePasswordStrength(request.getNewPassword());

        // 6. 设置新密码（@BCrypt自动散列）
        sysUser.setPassword(request.getNewPassword());

        // 7. 更新数据库
        this.updateById(sysUser);

        // 8. 清除Token（确保Token只能使用一次）
        emailService.clearResetToken(request.getEmail());

        log.info("通过Token重置密码成功，用户：{}, 邮箱：{}", sysUser.getUsername(), request.getEmail());

        return true;
    }

    /**
     * 获取用户角色集合
     * <p>
     * TODO: 实际项目中应该从用户角色表查询
     * 当前为简化实现，根据用户ID判断角色
     *
     * @param sysUser 用户实体
     * @return 角色集合
     */
    private Set<String> getUserRoles(SysUser sysUser) {
        Set<String> roles = new HashSet<>();

        // 简化实现：根据用户ID范围判断角色
        // 实际应该从user_roles表查询
        if (sysUser.getId() >= 9000000000000000000L) {
            // 管理员ID段
            roles.add("SUPER_ADMIN");
            roles.add("CONTENT_ADMIN");
        } else {
            // 普通用户
            roles.add("USER");
        }

        return roles;
    }

    /**
     * 获取邀请人用户名
     *
     * @return 邀请人用户名
     */
    @Override
    public boolean changeRole(ChangeRoleRequest request) {
        log.info("修改用户角色，username: {}, newRole: {}", request.getUsername(), request.getNewRole());

        // 1. 验证新角色是否有效
        try {
            UserRole.fromCode(request.getNewRole());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的角色代码: " + request.getNewRole());
        }

        // 2. 查询用户
        SysUser user = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 3. 更新角色
        user.setRole(request.getNewRole());
        int rows = baseMapper.updateById(user);

        return rows > 0;
    }

    private String getInviterUsername(Long inviterId) {
        try {
            SysUser inviter = this.getById(inviterId);
            if (inviter != null) {
                return inviter.getUsername();
            }
            log.warn("未找到邀请人信息: inviterId={}", inviterId);
            return null;
        } catch (Exception e) {
            log.error("查询邀请人用户名失败: inviterId={}, error={}", inviterId, e.getMessage());
            return null;
        }
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HTTP请求
     * @return IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个IP值，第一个是真实IP
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index);
            }
            return ip;
        }

        ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }
}
