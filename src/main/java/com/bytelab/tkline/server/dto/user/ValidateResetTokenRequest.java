package com.bytelab.tkline.server.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 验证重置密码Token请求
 * 
 * 用于验证用户点击邮件链接后，Token是否有效
 */
@Data
@Schema(description = "验证重置密码Token请求")
public class ValidateResetTokenRequest {
    
    @Schema(description = "用户名", required = true, example = "testuser")
    private String username;
    
    @Schema(description = "邮箱地址", required = true, example = "test@example.com")
    private String email;
    
    @Schema(description = "重置Token（从邮件链接中获取）", required = true)
    private String token;
}

