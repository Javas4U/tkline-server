package com.bytelab.tkline.server.dto.user;

import com.bytelab.tkline.server.dto.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 重置密码响应
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "重置密码响应")
public class ResetPasswordResponse extends BaseResponse {
    
    @Schema(description = "邮箱地址", example = "test1@example.com")
    private String email;
}

