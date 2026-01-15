package com.bytelab.tkline.server.service.core;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bytelab.tkline.server.dto.user.*;
import com.bytelab.tkline.server.entity.SysUser;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService extends IService<SysUser> {
    
    /**
     * 获取用户列表（分页）
     *
     * @param page 页码
     * @param size 每页大小
     * @return 用户列表
     */
    List<UserInfoDTO> getUserList(Integer page, Integer size);
    
    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    UserInfoDTO getUserById(Long id);
    
    /**
     * 创建用户
     * 需要解密前端传来的加密密码和敏感信息
     *
     * @param request 创建请求（包含加密数据）
     * @return 新创建的用户信息
     */
    UserInfoDTO createUser(CreateUserRequest request);
    
    /**
     * 更新用户信息
     *
     * @param request 更新请求（包含加密数据）
     * @return 更新后的用户信息
     */
    UserInfoDTO updateUser(UpdateUserRequest request);
    
    /**
     * 删除用户（逻辑删除）
     *
     * @param id 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long id);
    
    /**
     * 修改密码
     *
     * @param request 修改密码请求（包含加密密码）
     * @return 是否成功
     */
    boolean changePassword(ChangePasswordRequest request);
    
    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserInfoDTO getUserByUsername(String username);
    
    /**
     * 用户登录
     * 验证用户名密码，返回 JWT Token
     *
     * @param request 登录请求（包含加密密码）
     * @return 登录响应（包含 Token 和用户信息）
     */
    com.bytelab.tkline.server.dto.user.LoginResponse login(com.bytelab.tkline.server.dto.user.LoginRequest request);
    
    /**
     * 刷新Token
     * 延长用户会话时间
     *
     * @param oldToken 旧Token
     * @return 新的登录响应（包含新Token和用户信息）
     */
    com.bytelab.tkline.server.dto.user.LoginResponse refreshToken(String oldToken);
    
    /**
     * 邮箱验证码登录
     * 验证邮箱和验证码，返回 JWT Token
     *
     * @param request 邮箱登录请求（包含邮箱和验证码）
     * @return 登录响应（包含 Token 和用户信息）
     */
    com.bytelab.tkline.server.dto.user.LoginResponse emailLogin(com.bytelab.tkline.server.dto.user.EmailLoginRequest request);
    
    /**
     * 检查用户名是否已被注册
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean checkUsernameExists(String username);
    
    /**
     * 检查邮箱是否已注册
     *
     * @param email 邮箱地址
     * @return 是否存在
     */
    boolean checkEmailExists(String email);
    
    /**
     * 通过邮箱验证码重置密码
     * 忘记密码场景使用
     *
     * @param request 重置密码请求（包含邮箱、验证码、新密码）
     * @return 是否成功
     */
    boolean resetPassword(com.bytelab.tkline.server.dto.user.ResetPasswordRequest request);
    
    /**
     * 发送重置密码链接到邮箱
     * 
     * @param email 邮箱地址
     * @param resetUrl 重置密码页面URL
     */
    void sendResetPasswordLink(String email, String resetUrl);
    
    /**
     * 验证重置密码Token
     * 
     * @param request 验证请求（包含username、email、token）
     * @return 验证结果
     */
    com.bytelab.tkline.server.dto.user.ValidateResetTokenResponse validateResetToken(
            com.bytelab.tkline.server.dto.user.ValidateResetTokenRequest request);
    
    /**
     * 通过Token重置密码
     * 
     * @param request 重置密码请求（包含username、email、token、新密码）
     * @return 是否成功
     */
    boolean resetPasswordByToken(com.bytelab.tkline.server.dto.user.ResetPasswordByTokenRequest request);

    boolean changeRole(ChangeRoleRequest request);
}