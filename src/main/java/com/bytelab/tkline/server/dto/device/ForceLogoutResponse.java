package com.bytelab.tkline.server.dto.device;

import com.bytelab.tkline.server.dto.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 强制下线响应
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "强制下线响应")
public class ForceLogoutResponse extends BaseResponse {
    
    @Schema(description = "登录日志ID", example = "1")
    private Long loginLogId;
}

