package com.bytelab.tkline.server.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 验证重置密码Token响应
 * 
 * 返回验证结果和错误信息
 */
@Data
@Builder
@Schema(description = "验证重置密码Token响应")
public class ValidateResetTokenResponse {
    
    @Schema(description = "是否验证成功", example = "true")
    private Boolean valid;
    
    @Schema(description = "用户名（验证成功时返回）", example = "testuser")
    private String username;
    
    @Schema(description = "邮箱地址（验证成功时返回）", example = "test@example.com")
    private String email;
    
    @Schema(description = "错误类型代码（验证失败时返回，用于前端国际化）", 
            example = "user_not_found",
            allowableValues = {"user_not_found", "email_not_found", "email_not_match", "token_expired", "token_not_match"})
    private String errorType;
    
    @Schema(description = "提示信息（中文默认提示）", example = "验证成功，可以设置新密码")
    private String message;
}

