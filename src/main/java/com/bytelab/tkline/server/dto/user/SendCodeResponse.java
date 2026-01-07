package com.bytelab.tkline.server.dto.user;

import com.bytelab.tkline.server.dto.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 发送验证码响应
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发送验证码响应")
public class SendCodeResponse extends BaseResponse {
    
    @Schema(description = "邮箱地址", example = "test1@example.com")
    private String email;
    
    @Schema(description = "有效期（秒）", example = "300")
    private Integer expiresIn;
    
    @Schema(description = "验证码（仅Debug模式返回）", example = "123456")
    private String code;
}

