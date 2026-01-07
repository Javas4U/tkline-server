package com.bytelab.tkline.server.service.modules;

import com.bytelab.tkline.server.dto.user.SendCodeRequest;

/**
 * 邮箱服务接口
 */
public interface EmailService {
    
    /**
     * 发送验证码到邮箱（支持多种用途）
     * 
     * @param email 目标邮箱地址
     * @param purpose 验证码用途（LOGIN/RESET_PASSWORD等）
     * @return 验证码（6位数字）
     */
    String sendVerificationCode(String email, SendCodeRequest.CodePurpose purpose);
    
    /**
     * 验证邮箱验证码
     * 
     * @param email 邮箱地址
     * @param code 验证码
     * @param purpose 验证码用途（需要匹配）
     * @return 是否验证成功
     */
    boolean verifyCode(String email, String code, SendCodeRequest.CodePurpose purpose);
    
    /**
     * 清除验证码
     * 
     * @param email 邮箱地址
     * @param purpose 验证码用途
     */
    void clearCode(String email, SendCodeRequest.CodePurpose purpose);
    
    /**
     * 发送重置密码链接到邮箱
     * 
     * @param email 邮箱地址
     * @param username 用户名
     * @param resetUrl 重置密码页面URL
     * @return 重置Token
     */
    String sendResetPasswordLink(String email, String username, String resetUrl);
    
    /**
     * 验证重置密码Token
     * 
     * @param email 邮箱地址
     * @param token 重置Token
     * @return 是否有效
     */
    boolean verifyResetToken(String email, String token);
    
    /**
     * 验证重置密码Token并返回详细信息
     * 
     * @param username 用户名
     * @param email 邮箱地址
     * @param token 重置Token
     * @return Token详细信息（包括username），如果无效则返回null
     */
    ResetTokenInfo verifyResetTokenWithDetails(String username, String email, String token);
    
    /**
     * 清除重置Token
     * 
     * @param email 邮箱地址
     */
    void clearResetToken(String email);
    
    /**
     * 重置Token信息
     */
    class ResetTokenInfo {
        private final String username;
        private final String email;
        private final long expireTime;
        
        public ResetTokenInfo(String username, String email, long expireTime) {
            this.username = username;
            this.email = email;
            this.expireTime = expireTime;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getEmail() {
            return email;
        }
        
        public long getExpireTime() {
            return expireTime;
        }
    }
}

