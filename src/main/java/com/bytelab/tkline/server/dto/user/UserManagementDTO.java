package com.bytelab.tkline.server.dto.user;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户管理DTO（用于用户列表展示）
 * 
 * @author Apex Tunnel Team
 * @since 2025-10-28
 */
@Data
public class UserManagementDTO {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 角色代码（SUPER_ADMIN/ADMIN/USER）
     */
    private String role;
    
    /**
     * 角色显示名称
     */
    private String roleDisplay;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 创建人
     */
    private String createBy;
    
    /**
     * 更新人
     */
    private String updateBy;
}

