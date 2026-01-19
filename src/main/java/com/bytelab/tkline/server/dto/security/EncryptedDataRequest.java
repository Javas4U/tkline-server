package com.bytelab.tkline.server.dto.security;

import lombok.Data;

/**
 * 加密数据请求DTO
 * 前端发送加密数据时使用
 */
@Data
public class EncryptedDataRequest {

    /**
     * 密钥ID
     * 用于标识使用哪个公钥进行加密
     */
    private String keyId;

    /**
     * 加密后的数据（Base64编码）
     * 原始数据使用公钥加密后的结果
     */
    private String encryptedData;

    /**
     * 业务类型（可选）
     * 用于标识加密数据的用途，如：login_password, register_password等
     */
    private String businessType;
}

