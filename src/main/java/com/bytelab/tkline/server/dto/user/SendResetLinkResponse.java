package com.bytelab.tkline.server.dto.user;

import com.bytelab.tkline.server.dto.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 发送重置密码链接响应
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发送重置密码链接响应")
public class SendResetLinkResponse extends BaseResponse {
    
    @Schema(description = "邮箱地址", example = "test1@example.com")
    private String email;
    
    @Schema(description = "Token有效期（秒）", example = "300")
    private Integer expiresIn;
}

