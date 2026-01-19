package com.bytelab.tkline.server.dto.security;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 解密数据响应
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "解密数据响应")
public class DecryptDataResponse {
    
    @Schema(description = "解密后的明文数据", example = "test@example.com")
    private String decryptedData;
    
    @Schema(description = "业务类型", example = "email")
    private String businessType;
}

