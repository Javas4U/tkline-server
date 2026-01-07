package com.bytelab.tkline.server.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bytelab.tkline.server.converter.ProfileConverter;
import com.bytelab.tkline.server.dto.profile.ChangePasswordRequest;
import com.bytelab.tkline.server.dto.profile.ProfileInfoDTO;
import com.bytelab.tkline.server.entity.SysUser;
import com.bytelab.tkline.server.mapper.UserMapper;
import com.bytelab.tkline.server.service.user.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 个人中心服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserMapper userMapper;
    private final ProfileConverter profileConverter;

    @Override
    public ProfileInfoDTO getProfileInfo(Long userId) {
        log.info("获取个人中心信息，用户ID：{}", userId);
        
        // 获取用户信息
        SysUser sysUser = userMapper.selectById(userId);
        if (sysUser == null || sysUser.getDeleted() == 1) {
            log.warn("用户不存在：{}", userId);
            throw new RuntimeException("用户不存在");
        }
        
        // 转换为DTO
        ProfileInfoDTO profileInfoDTO = profileConverter.toProfileInfoDTO(sysUser);
        
        // 补充统计信息
        enrichProfileInfo(profileInfoDTO, userId);
        
        log.info("获取个人中心信息成功，用户：{}", sysUser.getUsername());
        return profileInfoDTO;
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("修改密码，用户ID：{}", userId);
        
        // 获取用户信息
        SysUser sysUser = userMapper.selectById(userId);
        if (sysUser == null || sysUser.getDeleted() == 1) {
            log.warn("用户不存在：{}", userId);
            throw new RuntimeException("用户不存在");
        }
        
        // 验证旧密码（这里简化处理，实际应该加密比较）
        // if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
        //     throw new RuntimeException("原密码错误");
        // }
        
        // 更新密码（这里简化处理，实际应该加密存储）
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .set(SysUser::getUpdateTime, LocalDateTime.now());
        // .set(User::getPassword, passwordEncoder.encode(request.getNewPassword()));
        
        int result = userMapper.update(null, updateWrapper);
        if (result > 0) {
            log.info("修改密码成功，用户ID：{}", userId);
        } else {
            log.error("修改密码失败，用户ID：{}", userId);
            throw new RuntimeException("修改密码失败");
        }
    }

    @Override
    public String resetSubscription(Long userId) {
        log.info("重置订阅，用户ID：{}", userId);

        // 获取用户信息
        SysUser sysUser = userMapper.selectById(userId);
        if (sysUser == null || sysUser.getDeleted() == 1) {
            log.warn("用户不存在：{}", userId);
            throw new RuntimeException("用户不存在");
        }

        // 生成新的订阅UUID (简化版,不再保存到inviteCode字段)
        String newUuid = "APEX" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        log.info("生成新的订阅UUID，用户ID：{}，新UUID：{}", userId, newUuid);
        return newUuid;
    }

    /**
     * 丰富个人中心信息
     */
    private void enrichProfileInfo(ProfileInfoDTO profileInfoDTO, Long userId) {
        // 设置设备数量（暂时设为0，后续可扩展）
        profileInfoDTO.setDeviceCount(0);

        // 设置邀请数量（暂时设为0）
        profileInfoDTO.setInviteCount(0);

        // 设置总佣金（暂时设为0）
        profileInfoDTO.setTotalCommission("0.00");

        // 设置默认头像
        if (profileInfoDTO.getAvatar() == null) {
            profileInfoDTO.setAvatar("/images/avatar/default.png");
        }

        log.debug("丰富个人中心信息完成");
    }
}