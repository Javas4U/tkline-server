package com.bytelab.tkline.server.dto.security;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 生成密钥对请求DTO
 */
@Data
public class GenerateKeyRequest {

    /**
     * 密钥长度（位）
     * 可选值：1024, 2048, 4096
     * 默认：2048
     */
    private Integer keySize = 2048;

    /**
     * 密钥描述
     */
    private String description;

    /**
     * 过期时间
     * 如果不指定，默认为30天后
     */
    private LocalDateTime expireTime;

    /**
     * 是否立即设置为活跃密钥
     * 默认：false
     */
    private Boolean setActive = false;
}

