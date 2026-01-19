package com.bytelab.tkline.server.dto.user;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 角色变更日志DTO
 * 
 * @author Apex Tunnel Team
 * @since 2025-10-28
 */
@Data
public class RoleChangeLogDTO {
    
    /**
     * 日志ID
     */
    private Long id;
    
    /**
     * 目标用户
     */
    private String targetUsername;
    
    /**
     * 操作人
     */
    private String operator;
    
    /**
     * 原角色
     */
    private String oldRole;
    
    /**
     * 原角色显示名称
     */
    private String oldRoleDisplay;
    
    /**
     * 新角色
     */
    private String newRole;
    
    /**
     * 新角色显示名称
     */
    private String newRoleDisplay;
    
    /**
     * 变更时间
     */
    private LocalDateTime changeTime;
    
    /**
     * 备注
     */
    private String remark;
}

