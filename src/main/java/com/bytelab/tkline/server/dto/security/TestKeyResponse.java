package com.bytelab.tkline.server.dto.security;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 测试密钥响应
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "测试密钥响应")
public class TestKeyResponse {
    
    @Schema(description = "密钥ID", example = "KEY-2024-10-14-001")
    private String keyId;
    
    @Schema(description = "密钥版本", example = "1")
    private Integer version;
    
    @Schema(description = "是否活跃", example = "true")
    private Boolean isActive;
    
    @Schema(description = "状态", example = "正常")
    private String status;
}

