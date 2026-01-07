package com.bytelab.tkline.server.service.core;

import com.bytelab.tkline.server.dto.security.GenerateKeyRequest;
import com.bytelab.tkline.server.dto.security.KeyPairInfoDTO;
import com.bytelab.tkline.server.dto.security.PublicKeyDTO;
import com.bytelab.tkline.server.entity.RsaKeyPair;

import java.util.List;

/**
 * 安全密钥管理服务接口
 */
public interface SecurityService {

    /**
     * 获取当前活跃的公钥
     *
     * @return 公钥信息
     */
    PublicKeyDTO getActivePublicKey();

    /**
     * 使用私钥解密数据
     *
     * @param keyId         密钥ID
     * @param encryptedData 加密数据（Base64编码）
     * @return 解密后的明文
     */
    String decryptData(String keyId, String encryptedData);
}

