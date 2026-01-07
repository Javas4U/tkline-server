package com.bytelab.tkline.server.dto.security;

import com.bytelab.tkline.server.dto.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 删除密钥响应
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "删除密钥响应")
public class DeleteKeyResponse extends BaseResponse {
    
    @Schema(description = "密钥ID", example = "KEY-2024-10-14-001")
    private String keyId;
}

