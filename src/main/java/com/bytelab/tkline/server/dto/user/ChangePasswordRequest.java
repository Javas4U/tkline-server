package com.bytelab.tkline.server.dto.user;

import com.bytelab.tkline.server.annotation.Decrypt;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 修改密码请求DTO
 * <p>
 * 使用@Decrypt注解自动解密
 */
@Data
@Schema(description = "修改密码请求")
public class ChangePasswordRequest {
    
    @Schema(description = "用户ID", example = "1")
    private Long userId;
    
    @Schema(description = "RSA密钥ID", example = "KEY-2024-10-14-001")
    private String keyId;
    
    /**
     * 旧密码（RSA加密，自动解密）
     */
    @Decrypt
    @Schema(description = "加密的旧密码（RSA加密）", example = "MIIBIjAN...")
    private String oldPassword;
    
    /**
     * 新密码（RSA加密，自动解密）
     */
    @Decrypt
    @Schema(description = "加密的新密码（RSA加密）", example = "MIIBIjAN...")
    private String newPassword;
}

