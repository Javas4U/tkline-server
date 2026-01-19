package com.bytelab.tkline.server.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 发送验证码请求
 */
@Data
@Schema(description = "发送验证码请求")
public class SendCodeRequest {
    
    @Schema(description = "邮箱地址", example = "user@example.com", required = true)
    private String email;
    
    /**
     * 验证码用途类型
     */
    @Schema(description = "验证码用途：LOGIN-邮箱登录，RESET_PASSWORD-重置密码", 
            example = "LOGIN", 
            required = true,
            allowableValues = {"LOGIN", "RESET_PASSWORD"})
    private CodePurpose purpose;
    
    /**
     * 验证码用途枚举
     */
    public enum CodePurpose {
        /**
         * 邮箱登录
         */
        LOGIN,
        
        /**
         * 重置密码
         */
        RESET_PASSWORD
    }
}
