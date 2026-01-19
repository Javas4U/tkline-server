package com.bytelab.tkline.server.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 登录响应DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    /**
     * JWT Token
     */
    private String token;
    
    /**
     * Token 类型
     */
    @lombok.Builder.Default
    private String tokenType = "Bearer";
    
    /**
     * Token 过期时间（毫秒时间戳）
     */
    private Long expiresAt;
    
    /**
     * 用户信息
     */
    private UserInfoDTO userInfo;
}

