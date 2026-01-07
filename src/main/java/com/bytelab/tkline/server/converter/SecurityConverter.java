package com.bytelab.tkline.server.converter;

import com.bytelab.tkline.server.dto.security.KeyPairInfoDTO;
import com.bytelab.tkline.server.dto.security.PublicKeyDTO;
import com.bytelab.tkline.server.entity.RsaKeyPair;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 安全相关 MapStruct 转换器
 */
@Mapper(componentModel = "spring")
public interface SecurityConverter {

    SecurityConverter INSTANCE = Mappers.getMapper(SecurityConverter.class);

    /**
     * RsaKeyPair 转 KeyPairInfoDTO
     * 注意：不包含私钥
     */
    @Mapping(target = "statusDesc", expression = "java(getStatusDesc(rsaKeyPair))")
    KeyPairInfoDTO toKeyPairInfoDTO(RsaKeyPair rsaKeyPair);

    /**
     * RsaKeyPair 转 PublicKeyDTO
     * 仅包含公钥和必要信息
     */
    @Mapping(source = "keyId", target = "keyId")
    @Mapping(source = "publicKey", target = "publicKey")
    PublicKeyDTO toPublicKeyDTO(RsaKeyPair rsaKeyPair);

    /**
     * 获取密钥状态描述
     */
    default String getStatusDesc(RsaKeyPair rsaKeyPair) {
        if (rsaKeyPair.getIsActive() == 1) {
            return "活跃";
        }
        LocalDateTime now = LocalDateTime.now();
        if (rsaKeyPair.getExpireTime() != null && now.isAfter(rsaKeyPair.getExpireTime())) {
            return "已过期";
        }
        return "非活跃";
    }

    /**
     * LocalDateTime 转时间戳（毫秒）
     */
    default Long toTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}

