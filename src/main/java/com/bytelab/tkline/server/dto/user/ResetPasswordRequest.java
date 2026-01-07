package com.bytelab.tkline.server.dto.user;

import com.bytelab.tkline.server.annotation.Decrypt;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 重置密码请求DTO
 * 
 * 通过邮箱验证码重置密码（忘记密码场景）
 */
@Data
@Schema(description = "重置密码请求")
public class ResetPasswordRequest {

    @Schema(description = "邮箱地址", example = "test1@example.com", required = true)
    private String username;

    @Schema(description = "邮箱地址", example = "test1@example.com", required = true)
    private String email;
    
    @Schema(description = "验证码（6位数字）", example = "123456", required = true)
    private String code;
    
    @Schema(description = "RSA密钥ID", example = "KEY-2024-10-14-001", required = true)
    private String keyId;
    
    /**
     * 新密码（RSA加密，自动解密）
     */
    @Decrypt
    @Schema(description = "加密的新密码（RSA加密）", example = "MIIBIjAN...", required = true)
    private String newPassword;
}

