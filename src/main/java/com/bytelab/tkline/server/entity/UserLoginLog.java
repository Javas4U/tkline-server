package com.bytelab.tkline.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户登录日志实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_login_log")
public class UserLoginLog extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 登录IP
     */
    @TableField("login_ip")
    private String loginIp;

    /**
     * 登录地点
     */
    @TableField("login_location")
    private String loginLocation;

    /**
     * 浏览器
     */
    @TableField("browser")
    private String browser;

    /**
     * 操作系统
     */
    @TableField("os")
    private String os;

    /**
     * 设备类型：PC/Mobile/Tablet
     */
    @TableField("device_type")
    private String deviceType;

    /**
     * 设备唯一标识
     */
    @TableField("device_id")
    private String deviceId;

    /**
     * 登录状态：0-失败，1-成功
     */
    @TableField("login_status")
    private Integer loginStatus;

    /**
     * 登录类型：PASSWORD-密码登录，TOKEN-Token登录
     */
    @TableField("login_type")
    private String loginType;

    /**
     * 失败原因
     */
    @TableField("fail_reason")
    private String failReason;

    /**
     * JWT Token
     */
    @TableField("token")
    private String token;

    /**
     * Token过期时间
     */
    @TableField("token_expire_time")
    private LocalDateTime tokenExpireTime;

    /**
     * 登出时间
     */
    @TableField("logout_time")
    private LocalDateTime logoutTime;

    /**
     * 在线时长（秒）
     */
    @TableField("online_duration")
    private Integer onlineDuration;
}

