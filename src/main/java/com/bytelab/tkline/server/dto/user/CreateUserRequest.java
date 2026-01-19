package com.bytelab.tkline.server.dto.user;

import com.bytelab.tkline.server.annotation.Decrypt;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 创建用户请求DTO
 * 
 * 使用@Decrypt注解实现自动解密：
 * - 前端使用RSA公钥加密敏感数据
 * - 后端接收后自动解密为明文
 * - Service层直接使用明文，无需手动解密
 */
@Data
@Schema(description = "创建用户请求")
public class CreateUserRequest {
    
    @Schema(description = "用户名", example = "testuser", required = true)
    private String username;
    
    @Schema(description = "RSA密钥ID", example = "KEY-2024-10-14-001", required = true)
    private String keyId;
    
    /**
     * 密码（RSA加密，自动解密）
     * 
     * 前端：使用公钥加密"password123"
     * 传输：密文"MIIBIjAN..."
     * 后端：自动解密为"password123"
     * Service：直接获取明文
     */
    @Decrypt
    @Schema(description = "加密的密码（RSA加密）", example = "MIIBIjAN...", required = true)
    private String password;
    
    /**
     * 邮箱（RSA加密，自动解密）
     */
    @Decrypt(required = false)
    @Schema(description = "加密的邮箱（RSA加密）", example = "MIIBIjAN...")
    private String email;
    
    /**
     * 手机号（RSA加密，自动解密）
     */
    @Decrypt(required = false)
    @Schema(description = "加密的手机号（RSA加密，可选）", example = "MIIBIjAN...")
    private String phone;
    
    /**
     * 身份证号（RSA加密，自动解密）
     */
    @Decrypt(required = false)
    @Schema(description = "加密的身份证号（RSA加密，可选）", example = "MIIBIjAN...")
    private String idCard;
    
    @Schema(description = "年龄", example = "25")
    private Integer age;
    
    @Schema(description = "邀请码", example = "APEX001")
    private String inviteCode;
}
