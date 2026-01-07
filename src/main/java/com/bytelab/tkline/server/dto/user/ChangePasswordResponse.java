package com.bytelab.tkline.server.dto.user;

import com.bytelab.tkline.server.dto.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 修改密码响应
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "修改密码响应")
public class ChangePasswordResponse extends BaseResponse {
    
    @Schema(description = "用户ID", example = "1")
    private Long userId;
}

