package com.bytelab.tkline.server.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 发送重置密码链接请求
 */
@Data
@Schema(description = "发送重置密码链接请求")
public class SendResetLinkRequest {
    
    @Schema(description = "邮箱地址", example = "test1@example.com", required = true)
    private String email;
    
    @Schema(description = "重置密码页面URL", example = "https://yourdomain.com/auth/reset-password", required = true)
    private String resetUrl;
}

