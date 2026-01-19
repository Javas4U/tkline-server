package com.bytelab.tkline.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bytelab.tkline.server.entity.RsaKeyPair;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * RSA密钥对 Mapper
 */
@Mapper
public interface RsaKeyPairMapper extends BaseMapper<RsaKeyPair> {

    /**
     * 将所有密钥设置为非活跃状态
     */
    @Update("UPDATE rsa_key_pair SET is_active = 0 WHERE deleted = 0")
    void deactivateAllKeys();

    /**
     * 更新密钥使用统计
     *
     * @param keyId 密钥ID
     */
    @Update("UPDATE rsa_key_pair SET usage_count = usage_count + 1, last_used_time = NOW() WHERE key_id = #{keyId} AND deleted = 0")
    void updateUsageStats(@Param("keyId") String keyId);
}

