package com.bytelab.tkline.server.service.core.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bytelab.tkline.server.converter.SecurityConverter;
import com.bytelab.tkline.server.dto.security.GenerateKeyRequest;
import com.bytelab.tkline.server.dto.security.PublicKeyDTO;
import com.bytelab.tkline.server.entity.RsaKeyPair;
import com.bytelab.tkline.server.mapper.RsaKeyPairMapper;
import com.bytelab.tkline.server.service.core.SecurityService;
import com.bytelab.tkline.server.util.RsaKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 安全密钥管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {

    private final RsaKeyPairMapper rsaKeyPairMapper;
    private final SecurityConverter securityConverter;

    @Override
    public PublicKeyDTO getActivePublicKey() {
        LambdaQueryWrapper<RsaKeyPair> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RsaKeyPair::getIsActive, 1)
                .orderByDesc(RsaKeyPair::getCreateTime)
                .last("LIMIT 1");

        RsaKeyPair activeKey = rsaKeyPairMapper.selectOne(wrapper);
        
        // 情况1: 没有活跃密钥，自动生成
        if (activeKey == null) {
            log.warn("未找到活跃的密钥对，尝试自动生成...");
            activeKey = generateAndActivateDefaultKey("系统自动生成的默认密钥");
        } 
        // 情况2: 有活跃密钥，但已过期，自动轮换
        else if (isKeyExpired(activeKey)) {
            log.warn("当前活跃密钥已过期，keyId: {}, expireTime: {}, 开始自动轮换...", 
                    activeKey.getKeyId(), activeKey.getExpireTime());
            
            // 将过期密钥设置为非活跃
            activeKey.setIsActive(0);
            rsaKeyPairMapper.updateById(activeKey);
            
            // 生成新的活跃密钥
            activeKey = generateAndActivateDefaultKey("自动轮换生成的密钥（旧密钥已过期）");
            
            log.info("密钥轮换完成，新密钥 keyId: {}, expireTime: {}", 
                    activeKey.getKeyId(), activeKey.getExpireTime());
        }

        return securityConverter.toPublicKeyDTO(activeKey);
    }

    /**
     * 生成并激活默认密钥
     * 此方法会创建一个新的2048位密钥，并立即设置为活跃状态
     *
     * @param description 密钥描述
     * @return 新生成的活跃密钥
     */
    private RsaKeyPair generateAndActivateDefaultKey(String description) {
        GenerateKeyRequest request = new GenerateKeyRequest();
        request.setKeySize(2048);
        request.setDescription(description);
        request.setSetActive(true);
        request.setExpireTime(LocalDateTime.now().plusDays(30)); // 默认30天过期

        return generateKeyPair(request);
    }

    private RsaKeyPair generateKeyPair(GenerateKeyRequest request) {
        // 生成密钥对
        Map<String, String> keyPair = RsaKeyUtil.generateKeyPair(request.getKeySize());
        String publicKey = keyPair.get("publicKey");
        String privateKey = keyPair.get("privateKey");

        // 验证密钥对
        if (!RsaKeyUtil.verifyKeyPair(publicKey, privateKey)) {
            throw new RuntimeException("生成的密钥对验证失败");
        }

        // 创建密钥对实体
        RsaKeyPair rsaKeyPair = new RsaKeyPair();
        rsaKeyPair.setKeyId(generateKeyId());
        rsaKeyPair.setPublicKey(publicKey);
        rsaKeyPair.setPrivateKey(privateKey); // TODO: 需要加密存储
        rsaKeyPair.setKeySize(request.getKeySize());
        rsaKeyPair.setAlgorithm("RSA");
        rsaKeyPair.setVersion(getNextVersion());
        rsaKeyPair.setIsActive(request.getSetActive() ? 1 : 0);
        rsaKeyPair.setExpireTime(request.getExpireTime() != null ?
                request.getExpireTime() : LocalDateTime.now().plusDays(30));
        rsaKeyPair.setUsageCount(0L);
        rsaKeyPair.setDescription(request.getDescription());

        // 如果设置为活跃，先将其他所有密钥设置为非活跃
        if (request.getSetActive()) {
            rsaKeyPairMapper.deactivateAllKeys();
        }

        // 保存密钥对
        rsaKeyPairMapper.insert(rsaKeyPair);

        log.info("成功生成密钥对，keyId: {}, version: {}, isActive: {}",
                rsaKeyPair.getKeyId(), rsaKeyPair.getVersion(), rsaKeyPair.getIsActive());

        return rsaKeyPair;
    }

    /**
     * 检查密钥是否已过期
     *
     * @param keyPair 密钥对
     * @return true-已过期，false-未过期
     */
    private boolean isKeyExpired(RsaKeyPair keyPair) {
        if (keyPair.getExpireTime() == null) {
            // 如果没有设置过期时间，则认为永不过期
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        boolean expired = now.isAfter(keyPair.getExpireTime());

        if (expired) {
            log.warn("密钥已过期 - keyId: {}, version: {}, expireTime: {}, now: {}",
                    keyPair.getKeyId(), keyPair.getVersion(), keyPair.getExpireTime(), now);
        }

        return expired;
    }

    /**
     * 生成密钥ID
     */
    private String generateKeyId() {
        return "RSA-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    /**
     * 获取下一个版本号
     */
    private Integer getNextVersion() {
        LambdaQueryWrapper<RsaKeyPair> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(RsaKeyPair::getVersion)
                .orderByDesc(RsaKeyPair::getVersion)
                .last("LIMIT 1");

        RsaKeyPair latestKey = rsaKeyPairMapper.selectOne(wrapper);
        return latestKey != null ? latestKey.getVersion() + 1 : 1;
    }

    @Override
    public String decryptData(String keyId, String encryptedData) {
        // 查询密钥对
        LambdaQueryWrapper<RsaKeyPair> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RsaKeyPair::getKeyId, keyId);
        RsaKeyPair keyPair = rsaKeyPairMapper.selectOne(wrapper);

        if (keyPair == null) {
            throw new RuntimeException("未找到指定的密钥，keyId: " + keyId);
        }

        // 检查密钥是否过期
        if (keyPair.getExpireTime() != null &&
                LocalDateTime.now().isAfter(keyPair.getExpireTime())) {
            log.warn("使用已过期的密钥进行解密，keyId: {}", keyId);
        }

        // 使用私钥解密
        String decryptedData = RsaKeyUtil.decrypt(encryptedData, keyPair.getPrivateKey());

        // 更新使用统计
        rsaKeyPairMapper.updateUsageStats(keyId);

        log.debug("成功解密数据，keyId: {}", keyId);
        return decryptedData;
    }
}

