package com.bytelab.tkline.server.dto.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公钥响应DTO
 * 返回给前端用于加密敏感信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicKeyDTO {

    /**
     * 密钥唯一标识
     * 前端在发送加密数据时需要携带此ID
     */
    private String keyId;

    /**
     * RSA公钥（Base64编码）
     * 前端使用此公钥加密敏感数据
     */
    private String publicKey;
}

