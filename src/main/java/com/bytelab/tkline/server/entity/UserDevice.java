package com.bytelab.tkline.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户设备实体类
 * <p>
 * 字段加密说明：
 * - device_id, last_ip: 明文存储（便于查询和调试）
 * - 如需加密，使用@Encrypt注解 + EncryptTypeHandler
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_device")
public class UserDevice extends BaseEntity {

    /**
     * 设备ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 设备名称
     */
    @TableField("device_name")
    private String deviceName;

    /**
     * 设备类型：IOS,ANDROID,WINDOWS,MAC
     */
    @TableField("device_type")
    private String deviceType;

    /**
     * 设备唯一标识
     */
    @TableField("device_id")
    private String deviceId;

    /**
     * 设备型号
     */
    @TableField("device_model")
    private String deviceModel;

    /**
     * 操作系统版本
     */
    @TableField("os_version")
    private String osVersion;

    /**
     * 应用版本
     */
    @TableField("app_version")
    private String appVersion;

    /**
     * 最后在线时间
     */
    @TableField("last_online_time")
    private LocalDateTime lastOnlineTime;

    /**
     * 最后连接IP
     */
    @TableField("last_ip")
    private String lastIp;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

}
