package com.bytelab.tkline.server.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 邮箱验证码登录请求
 */
@Data
@Schema(description = "邮箱验证码登录请求")
public class EmailLoginRequest {
    
    @Schema(description = "邮箱地址", example = "test1@example.com", required = true)
    private String email;
    
    @Schema(description = "验证码（6位数字）", example = "123456", required = true)
    private String code;
    
    @Schema(description = "设备信息（可选）", example = "iPhone 15 Pro")
    private String deviceInfo;
}

