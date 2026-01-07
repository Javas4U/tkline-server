package com.bytelab.tkline.server.dto.user;

import com.bytelab.tkline.server.annotation.Decrypt;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新用户请求DTO
 * 
 * 使用@Decrypt注解自动解密
 */
@Data
@Schema(description = "更新用户请求")
public class UpdateUserRequest {
    
    @Schema(description = "用户ID", example = "1", required = true)
    private Long id;
    
    @Schema(description = "RSA密钥ID", example = "KEY-2024-10-14-001")
    private String keyId;
    
    /**
     * 邮箱（可选，RSA加密，自动解密）
     */
    @Decrypt(required = false)
    @Schema(description = "加密的邮箱（RSA加密，可选）", example = "MIIBIjAN...")
    private String email;
    
    /**
     * 手机号（可选，RSA加密，自动解密）
     */
    @Decrypt(required = false)
    @Schema(description = "加密的手机号（RSA加密，可选）", example = "MIIBIjAN...")
    private String phone;
    
    /**
     * 身份证号（可选，RSA加密，自动解密）
     */
    @Decrypt(required = false)
    @Schema(description = "加密的身份证号（RSA加密，可选）", example = "MIIBIjAN...")
    private String idCard;
    
    @Schema(description = "年龄", example = "25")
    private Integer age;
    
    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;
    
    @Schema(description = "订阅类型", example = "PREMIUM")
    private String subscriptionType;
}

