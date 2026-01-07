package com.bytelab.tkline.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * RSA密钥对实体
 * 用于存储和管理系统的RSA公私钥对
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rsa_key_pair")
public class RsaKeyPair extends BaseEntity {

    /**
     * 密钥对ID（自增主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 密钥唯一标识（UUID或自定义格式）
     */
    @TableField("key_id")
    private String keyId;

    /**
     * RSA公钥（Base64编码）
     * 可以公开，用于前端加密敏感数据
     */
    @TableField("public_key")
    private String publicKey;

    /**
     * RSA私钥（Base64编码，加密存储）
     * 需要严格保密，仅后端使用，用于解密数据
     */
    @TableField("private_key")
    private String privateKey;

    /**
     * 密钥长度（位）
     * 常用值：1024, 2048, 4096
     * 推荐使用2048位以上
     */
    @TableField("key_size")
    private Integer keySize;

    /**
     * 加密算法
     * 默认：RSA
     */
    @TableField("algorithm")
    private String algorithm;

    /**
     * 密钥版本号
     * 用于密钥轮换时的版本管理
     */
    @TableField("version")
    private Integer version;

    /**
     * 是否为活跃密钥
     * 0-否，1-是
     * 同一时间只能有一个活跃密钥
     */
    @TableField("is_active")
    private Integer isActive;

    /**
     * 过期时间
     * 超过此时间后，密钥应被轮换
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 最后使用时间
     * 用于统计和监控
     */
    @TableField("last_used_time")
    private LocalDateTime lastUsedTime;

    /**
     * 使用次数
     * 用于统计和监控
     */
    @TableField("usage_count")
    private Long usageCount;

    /**
     * 密钥描述
     */
    @TableField("description")
    private String description;
}

