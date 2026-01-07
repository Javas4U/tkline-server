package com.bytelab.tkline.server.service.user;

import com.bytelab.tkline.server.dto.profile.ChangePasswordRequest;
import com.bytelab.tkline.server.dto.profile.ProfileInfoDTO;

/**
 * 个人中心服务接口
 */
public interface ProfileService {
    
    /**
     * 获取个人中心信息
     */
    ProfileInfoDTO getProfileInfo(Long userId);
    
    /**
     * 修改密码
     */
    void changePassword(Long userId, ChangePasswordRequest request);
    
    /**
     * 重置订阅
     */
    String resetSubscription(Long userId);
}
