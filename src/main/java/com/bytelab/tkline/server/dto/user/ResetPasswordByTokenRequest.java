package com.bytelab.tkline.server.dto.user;

import com.bytelab.tkline.server.annotation.Decrypt;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 通过Token重置密码请求DTO
 * 
 * 用于用户点击邮件链接后，通过Token重置密码
 */
@Data
@Schema(description = "通过Token重置密码请求")
public class ResetPasswordByTokenRequest {
    
    @Schema(description = "用户名", example = "testuser", required = true)
    private String username;
    
    @Schema(description = "邮箱地址", example = "test@example.com", required = true)
    private String email;
    
    @Schema(description = "重置Token（从邮件链接中获取）", required = true)
    private String token;
    
    @Schema(description = "RSA密钥ID", example = "KEY-2024-10-14-001", required = true)
    private String keyId;
    
    /**
     * 新密码（RSA加密，自动解密）
     */
    @Decrypt
    @Schema(description = "加密的新密码（RSA加密）", example = "MIIBIjAN...", required = true)
    private String newPassword;
}

