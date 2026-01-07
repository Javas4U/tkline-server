package com.bytelab.tkline.server.dto.profile;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 个人中心信息DTO
 */
@Data
public class ProfileInfoDTO {
    // 用户基本信息
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String avatar;
    
    // 订阅信息
    private String subscriptionType;
    private LocalDateTime subscriptionExpireTime;
    private String inviteCode;
    
    // 流量信息
    private Long totalTraffic;
    private Long monthlyTrafficLimit;
    private Integer maxDevices;
    
    // 统计信息
    private LocalDateTime registrationTime;
    private Integer deviceCount;
    private Integer inviteCount;
    private String totalCommission;
    
    // 设置信息
    private Object notificationSettings;
    private Object securitySettings;
}