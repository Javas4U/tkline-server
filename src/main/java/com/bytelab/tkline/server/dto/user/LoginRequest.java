package com.bytelab.tkline.server.dto.user;

import com.bytelab.tkline.server.annotation.Decrypt;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录请求DTO
 * 
 * 使用@Decrypt注解自动解密密码
 */
@Data
@Schema(description = "用户登录请求")
public class LoginRequest {
    
    @Schema(description = "用户名", example = "testuser1", required = true)
    private String username;
    
    @Schema(description = "RSA密钥ID", example = "KEY-2024-10-14-001", required = true)
    private String keyId;
    
    /**
     * 密码（RSA加密，自动解密）
     */
    @Decrypt
    @Schema(description = "加密的密码（RSA加密）", example = "MIIBIjAN...", required = true)
    private String password;
}

