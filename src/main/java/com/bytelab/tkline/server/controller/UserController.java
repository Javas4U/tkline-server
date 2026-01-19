package com.bytelab.tkline.server.controller;

import com.bytelab.tkline.server.common.ApiResult;
import com.bytelab.tkline.server.dto.user.*;
import com.bytelab.tkline.server.exception.BusinessException;
import com.bytelab.tkline.server.service.core.UserService;
import com.bytelab.tkline.server.service.modules.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 * <p>
 * 核心功能：
 * 1. 用户 CRUD 操作
 * 2. 集成 RSA 密钥解密（密码、邮箱、手机号等敏感信息）
 * 3. 使用 BCrypt 存储密码散列值
 * 4. 符合 MVC 架构规范
 */
@Slf4j
@Tag(name = "用户管理", description = "用户管理接口，支持 RSA 加密传输")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    /**
     * 用户登录
     * <p>
     * 前端需要：
     * 1. 先调用 /api/security/keys/public-key 获取公钥
     * 2. 使用公钥加密密码
     * 3. 将 keyId 和加密后的密码发送
     * <p>
     * 返回：
     * - token: JWT Token（30分钟有效期）
     * - tokenType: "Bearer"
     * - expiresAt: 过期时间戳
     * - userInfo: 用户信息
     */
    @Operation(summary = "用户登录", description = "用户登录验证，返回 JWT Token（有效期30分钟）")
    @PostMapping("/login")
    @PermitAll
    public ApiResult<LoginResponse> login(@RequestBody LoginRequest request) {
        log.info("登录请求，username: {}", request.getUsername());

        // 全局异常处理器会捕获异常，无需try-catch
        LoginResponse response = userService.login(request);

        log.info("登录成功，username: {}", request.getUsername());
        return ApiResult.success(response);
    }

    /**
     * 获取用户列表（分页）
     */
    @Operation(summary = "获取用户列表", description = "分页获取用户列表")
    @GetMapping("/list")
    public ApiResult<List<UserInfoDTO>> getUserList(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size) {

        log.info("查询用户列表，page: {}, size: {}", page, size);
        List<UserInfoDTO> users = userService.getUserList(page, size);
        return ApiResult.success(users);
    }

    /**
     * 根据ID获取用户详情
     */
    @Operation(summary = "获取用户详情", description = "根据ID获取用户详细信息")
    @GetMapping("/{id}")
    public ApiResult<UserInfoDTO> getUserById(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {

        log.info("查询用户详情，ID: {}", id);
        UserInfoDTO user = userService.getUserById(id);
        return ApiResult.success(user);
    }

    /**
     * 根据用户名获取用户
     */
    @Operation(summary = "根据用户名获取用户", description = "根据用户名查询用户信息")
    @GetMapping("/username/{username}")
    public ApiResult<UserInfoDTO> getUserByUsername(
            @Parameter(description = "用户名", required = true) @PathVariable String username) {

        log.info("根据用户名查询用户，username: {}", username);
        UserInfoDTO user = userService.getUserByUsername(username);
        return ApiResult.success(user);
    }

    /**
     * 创建用户
     * <p>
     * 前端需要：
     * 1. 先调用 /api/security/keys/public-key 获取公钥
     * 2. 使用公钥加密密码、邮箱、手机号等敏感信息
     * 3. 将 keyId 和加密后的数据一起发送
     */
    @Operation(summary = "创建用户", description = "创建新用户（密码等敏感信息需使用 RSA 公钥加密后传输）")
    @PostMapping("/create")
    public ApiResult<UserInfoDTO> createUser(@RequestBody CreateUserRequest request) {
        log.info("创建用户，username: {}", request.getUsername());

        UserInfoDTO user = userService.createUser(request);
        return ApiResult.success(user);
    }

    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息", description = "更新用户信息（敏感信息需使用 RSA 公钥加密后传输）")
    @PostMapping("/update")
    public ApiResult<UserInfoDTO> updateUser(@RequestBody UpdateUserRequest request) {
        log.info("更新用户，id: {}", request.getId());

        UserInfoDTO user = userService.updateUser(request);
        return ApiResult.success(user);
    }

    /**
     * 删除用户（逻辑删除）
     */
    @Operation(summary = "删除用户", description = "逻辑删除用户")
    @PostMapping("/delete")
    public ApiResult<DeleteUserResponse> deleteUser(@RequestParam Long id) {
        log.info("删除用户，id: {}", id);

        boolean success = userService.deleteUser(id);

        DeleteUserResponse response = DeleteUserResponse.builder()
                .success(success)
                .userId(id)
                .message("删除成功")
                .build();

        return ApiResult.success(response);
    }

    /**
     * 修改密码
     * <p>
     * 前端需要：
     * 1. 获取公钥
     * 2. 使用公钥分别加密旧密码和新密码
     * 3. 发送加密后的密码
     */
    @Operation(summary = "修改密码", description = "修改用户密码（旧密码和新密码都需使用 RSA 公钥加密后传输）")
    @PostMapping("/change-password")
    public ApiResult<ChangePasswordResponse> changePassword(@RequestBody ChangePasswordRequest request) {
        log.info("修改密码，userId: {}", request.getUserId());

        boolean success = userService.changePassword(request);

        ChangePasswordResponse response = ChangePasswordResponse.builder()
                .success(success)
                .userId(request.getUserId())
                .message("密码修改成功")
                .build();

        return ApiResult.success(response);
    }

    /**
     * 刷新Token
     * <p>
     * 适用场景：
     * - Token即将过期（5分钟内）
     * - 自动延长用户会话
     * <p>
     * 前端可在以下场景调用：
     * 1. 定时检查Token过期时间
     * 2. API请求返回401时尝试刷新
     * 3. 用户活跃时自动刷新
     */
    @Operation(summary = "刷新Token", description = "刷新JWT Token，延长会话时间（30分钟）")
    @PostMapping("/refresh-token")
    public ApiResult<LoginResponse> refreshToken(
            @Parameter(description = "当前Token（Bearer格式或纯Token）", required = true) @RequestHeader("Authorization") String authorization) {
        log.info("Token刷新请求");

        // 提取Token（去掉Bearer前缀）
        String token = authorization;
        if (authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }

        // 刷新Token并获取用户信息
        LoginResponse response = userService.refreshToken(token);

        log.info("Token刷新成功");
        return ApiResult.success(response);
    }

    /**
     * 检查用户名是否已被注册
     * <p>
     * 适用场景：
     * - 用户注册时实时验证用户名
     * - 表单验证
     * <p>
     * 特点：
     * - 公开接口，无需登录
     * - 返回 true/false 布尔值
     */
    @Operation(summary = "检查用户名是否已被注册", description = "验证用户名是否已存在，返回true表示已存在，false表示可用")
    @GetMapping("/check-username")
    public ApiResult<Boolean> checkUsernameExists(
            @Parameter(description = "用户名", required = true, example = "testuser") @RequestParam String username) {
        log.info("检查用户名是否存在，username: {}", username);

        boolean exists = userService.checkUsernameExists(username);

        log.debug("用户名存在性：{}, exists: {}", username, exists);
        return ApiResult.success(exists);
    }

    /**
     * 检查邮箱是否已被注册
     * <p>
     * 适用场景：
     * - 用户注册时实时验证邮箱
     * - 密码重置前验证邮箱
     * - 表单验证
     * <p>
     * 特点：
     * - 公开接口，无需登录
     * - 返回 true/false 布尔值
     */
    @Operation(summary = "检查邮箱是否已被注册", description = "验证邮箱是否已存在，返回true表示已存在，false表示可用")
    @GetMapping("/check-email")
    public ApiResult<Boolean> checkEmailExists(
            @Parameter(description = "邮箱地址", required = true, example = "test@example.com") @RequestParam String email) {
        log.info("检查邮箱是否存在，email: {}", email);

        boolean exists = userService.checkEmailExists(email);

        log.debug("邮箱存在性：{}, exists: {}", email, exists);
        return ApiResult.success(exists);
    }

    /**
     * 发送邮箱验证码
     * <p>
     * 适用场景：
     * - 邮箱验证码登录
     * - 邮箱验证
     * <p>
     * 限制：
     * - 同一邮箱1分钟内只能发送1次
     * - 验证码有效期5分钟
     */
    @Operation(summary = "发送邮箱验证码", description = "发送6位数字验证码到指定邮箱，有效期5分钟。发送前会验证邮箱是否已注册。")
    @PostMapping("/send-code")
    public ApiResult<SendCodeResponse> sendVerificationCode(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "发送验证码请求", required = true) @RequestBody SendCodeRequest request) {
        log.info("发送验证码，email: {}, purpose: {}", request.getEmail(), request.getPurpose());

        // 1. 验证邮箱格式
        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new com.bytelab.tkline.server.exception.BusinessException("邮箱格式不正确");
        }

        // 2. 验证用途
        if (request.getPurpose() == null) {
            throw new com.bytelab.tkline.server.exception.BusinessException("请指定验证码用途");
        }

        // 3. 根据用途验证邮箱是否已注册
        if (request.getPurpose() == SendCodeRequest.CodePurpose.LOGIN ||
                request.getPurpose() == SendCodeRequest.CodePurpose.RESET_PASSWORD) {
            boolean emailExists = userService.checkEmailExists(request.getEmail());
            if (!emailExists) {
                throw new com.bytelab.tkline.server.exception.BusinessException("该邮箱未注册");
            }
        }

        // 4. 发送验证码（由EmailService处理频率限制）
        String code = emailService.sendVerificationCode(request.getEmail(), request.getPurpose());

        // 5. 构建响应
        SendCodeResponse response = SendCodeResponse.builder()
                .email(request.getEmail())
                .message("验证码已发送")
                .expiresIn(300)
                .code(log.isDebugEnabled() ? code : null)
                .build();

        log.info("验证码发送成功");
        return ApiResult.success(response);
    }

    /**
     * 邮箱验证码登录
     * <p>
     * 流程：
     * 1. 前端调用 /send-code 发送验证码（purpose=LOGIN）
     * 2. 用户输入邮箱和验证码
     * 3. 调用本接口进行登录
     * 4. 返回JWT Token
     * <p>
     * 优势：
     * - 无需记忆密码
     * - 安全性高（验证码5分钟过期）
     * - 用户体验好
     */
    @Operation(summary = "邮箱验证码登录", description = "使用邮箱和验证码登录，返回JWT Token（30分钟有效期）")
    @PostMapping("/email-login")
    public ApiResult<LoginResponse> emailLogin(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "邮箱登录请求（包含邮箱和验证码）", required = true) @RequestBody EmailLoginRequest request) {
        log.info("邮箱验证码登录请求，邮箱：{}", request.getEmail());

        // 全局异常处理器会捕获异常，无需try-catch
        LoginResponse response = userService.emailLogin(request);

        log.info("邮箱登录成功，邮箱：{}", request.getEmail());
        return ApiResult.success(response);
    }

    /**
     * 通过邮箱验证码重置密码
     * <p>
     * 适用场景：
     * - 忘记密码
     * - 需要重置密码
     * <p>
     * 流程：
     * 1. 前端调用 /send-code 发送验证码（purpose=RESET_PASSWORD）
     * 2. 用户输入邮箱、验证码和新密码
     * 3. 调用本接口重置密码
     * 4. 重置成功后需要重新登录
     * <p>
     * 安全性：
     * - 验证码5分钟过期
     * - 需要邮箱验证（防止他人重置）
     * - 新密码RSA加密传输
     * - 新密码BCrypt散列存储
     */
    @Operation(summary = "邮箱验证码重置密码", description = "通过邮箱验证码重置密码（忘记密码场景）")
    @PostMapping("/reset-password")
    public ApiResult<ResetPasswordResponse> resetPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "重置密码请求（包含邮箱、验证码、新密码）", required = true) @RequestBody ResetPasswordRequest request) {
        log.info("重置密码请求，邮箱：{}", request.getEmail());

        // 全局异常处理器会捕获异常，无需try-catch
        boolean success = userService.resetPassword(request);

        ResetPasswordResponse response = ResetPasswordResponse.builder()
                .success(success)
                .email(request.getEmail())
                .message("密码重置成功，请使用新密码登录")
                .build();

        log.info("密码重置成功，邮箱：{}", request.getEmail());
        return ApiResult.success(response);
    }

    /**
     * 发送重置密码链接
     * <p>
     * 适用场景：
     * - 忘记密码（推荐方式）
     * - 通过邮件链接重置密码
     * <p>
     * 流程：
     * 1. 前端调用本接口（提供邮箱和重置页面URL）
     * 2. 后端生成5分钟有效的重置Token
     * 3. 发送包含Token的重置链接到用户邮箱
     * 4. 用户点击邮件中的链接
     * 5. 跳转到重置页面（携带username、email、token参数）
     * 6. 用户设置新密码
     * 7. 提交新密码和Token完成重置
     * <p>
     * 安全性：
     * - Token 5分钟过期
     * - Token仅可使用一次
     * - 链接包含用户信息防止误用
     * - 邮箱验证确保本人操作
     */
    @Operation(summary = "发送重置密码链接", description = "发送包含重置Token的链接到用户邮箱（5分钟有效）")
    @PostMapping("/send-reset-link")
    public ApiResult<SendResetLinkResponse> sendResetPasswordLink(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "发送重置链接请求", required = true) @RequestBody SendResetLinkRequest request) {
        log.info("发送重置密码链接，email: {}", request.getEmail());

        // 1. 验证邮箱格式
        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new com.bytelab.tkline.server.exception.BusinessException("邮箱格式不正确");
        }

        // 2. 验证resetUrl不为空
        if (request.getResetUrl() == null || request.getResetUrl().isEmpty()) {
            throw new com.bytelab.tkline.server.exception.BusinessException("重置页面URL不能为空");
        }

        // 3. 发送重置链接
        userService.sendResetPasswordLink(request.getEmail(), request.getResetUrl());

        // 4. 构建响应
        SendResetLinkResponse response = SendResetLinkResponse.builder()
                .email(request.getEmail())
                .expiresIn(300) // 5分钟
                .success(true)
                .message("重置密码链接已发送到您的邮箱，请务必在5分钟内完成密码重置操作！")
                .build();

        log.info("重置密码链接发送成功");
        return ApiResult.success(response);
    }

    /**
     * 验证重置密码Token
     * <p>
     * 适用场景：
     * - 用户点击邮件中的重置链接后，首先验证链接是否有效
     * - 在用户输入新密码前进行预验证
     * <p>
     * 流程：
     * 1. 用户点击邮件链接，携带username、email、token参数
     * 2. 前端调用本接口验证Token是否有效
     * 3. 验证通过后，前端显示重置密码表单
     * 4. 验证失败，前端根据errorType显示国际化的错误提示
     * <p>
     * 验证内容：
     * - 用户名是否存在
     * - 邮箱是否存在
     * - 用户名和邮箱是否属于同一个用户（是否一致）
     * - Token是否有效（未过期、未使用）
     * - Token是否与该用户名和邮箱匹配
     * <p>
     * 返回说明：
     * - HTTP 200：正常业务响应
     * - response.valid = true：验证成功
     * - response.valid = false：验证失败，通过errorType区分具体错误
     * <p>
     * 错误类型枚举（用于前端国际化）：
     * - "user_not_found": 用户不存在
     * - "email_not_found": 邮箱不存在
     * - "email_not_match": 邮箱与用户名不匹配
     * - "token_expired": Token过期或无效
     * - "token_not_match": Token与用户信息不匹配
     * <p>
     * 前端示例：
     * ```javascript
     * const result = await validateResetToken({ username, email, token });
     * if (result.data.valid) {
     * // 验证成功，显示重置表单
     * showResetForm();
     * } else {
     * // 验证失败，根据errorType显示国际化提示
     * const i18nKey = `reset.error.${result.data.errorType}`;
     * showError(t(i18nKey)); // 使用i18n翻译
     * }
     * ```
     */
    @Operation(summary = "验证重置密码Token", description = "验证重置密码链接是否有效，返回errorType用于前端国际化（用户点击邮件链接后调用）")
    @PostMapping("/validate-reset-token")
    public ApiResult<ValidateResetTokenResponse> validateResetToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "验证Token请求（包含username、email、token）", required = true) @RequestBody ValidateResetTokenRequest request) {
        log.info("验证重置Token，用户名：{}, 邮箱：{}", request.getUsername(), request.getEmail());

        // 1. 参数校验
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }

        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new BusinessException("邮箱不能为空");
        }

        if (request.getToken() == null || request.getToken().isEmpty()) {
            throw new BusinessException("Token不能为空");
        }

        // 2. 调用服务层验证，返回包含errorType的响应
        ValidateResetTokenResponse response = userService.validateResetToken(request);

        if (response.getValid()) {
            log.info("重置Token验证成功");
        } else {
            log.warn("重置Token验证失败，错误类型：{}", response.getErrorType());
        }

        // 3. 无论成功失败都返回HTTP 200，前端根据valid字段判断
        return ApiResult.success(response);
    }

    /**
     * 通过Token重置密码
     * <p>
     * 适用场景：
     * - 用户点击邮件链接后，验证Token通过，设置新密码
     * - 完成忘记密码流程的最后一步
     * <p>
     * 流程：
     * 1. 前端先调用 /validate-reset-token 验证Token
     * 2. 验证通过后，用户输入新密码
     * 3. 前端获取RSA公钥，加密新密码
     * 4. 调用本接口提交新密码
     * 5. 后端验证Token、更新密码、清除Token
     * 6. 重置成功，用户可使用新密码登录
     * <p>
     * 安全性：
     * - Token验证确保请求合法性
     * - 新密码RSA加密传输
     * - 新密码BCrypt散列存储
     * - Token重置后自动清除（只能使用一次）
     * - 密码强度检查
     */
    @Operation(summary = "通过Token重置密码", description = "使用重置Token设置新密码（Token使用后自动失效）")
    @PostMapping("/reset-password-by-token")
    public ApiResult<ResetPasswordResponse> resetPasswordByToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "重置密码请求（包含username、email、token、新密码）", required = true) @RequestBody ResetPasswordByTokenRequest request) {
        log.info("通过Token重置密码，用户名：{}, 邮箱：{}", request.getUsername(), request.getEmail());

        // 1. 参数校验
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            throw new com.bytelab.tkline.server.exception.BusinessException("用户名不能为空");
        }

        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new com.bytelab.tkline.server.exception.BusinessException("邮箱不能为空");
        }

        if (request.getToken() == null || request.getToken().isEmpty()) {
            throw new com.bytelab.tkline.server.exception.BusinessException("Token不能为空");
        }

        // 2. 调用服务层重置密码（全局异常处理器会捕获BusinessException）
        boolean success = userService.resetPasswordByToken(request);

        // 3. 构建响应
        ResetPasswordResponse response = ResetPasswordResponse.builder()
                .success(success)
                .email(request.getEmail())
                .message("密码重置成功，请使用新密码登录")
                .build();

        log.info("通过Token重置密码成功，用户名：{}", request.getUsername());
        return ApiResult.success(response);
    }

    @Operation(summary = "修改用户角色", description = "修改用户的角色（仅超级管理员可操作）")
    @PostMapping("/change-role")
    public ApiResult<Boolean> changeRole(@RequestBody ChangeRoleRequest request) {
        log.info("修改用户角色，request: {}", request);
        return ApiResult.success(userService.changeRole(request));
    }
}
