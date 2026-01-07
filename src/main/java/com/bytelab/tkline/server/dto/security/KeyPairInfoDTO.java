package com.bytelab.tkline.server.dto.security;

import com.bytelab.tkline.server.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 密钥对信息DTO
 * 用于管理后台查看密钥列表（不包含私钥）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KeyPairInfoDTO extends BaseDTO {

    /**
     * 密钥对ID
     */
    private Long id;

    /**
     * 密钥唯一标识
     */
    private String keyId;

    /**
     * RSA公钥（Base64编码）
     */
    private String publicKey;

    /**
     * 密钥长度（位）
     */
    private Integer keySize;

    /**
     * 加密算法
     */
    private String algorithm;

    /**
     * 密钥版本号
     */
    private Integer version;

    /**
     * 是否为活跃密钥
     */
    private Integer isActive;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedTime;

    /**
     * 使用次数
     */
    private Long usageCount;

    /**
     * 密钥描述
     */
    private String description;

    /**
     * 密钥状态描述
     */
    private String statusDesc;
}

