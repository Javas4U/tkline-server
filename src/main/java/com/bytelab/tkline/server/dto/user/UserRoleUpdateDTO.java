package com.bytelab.tkline.server.dto.user;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 用户角色更新DTO
 * 
 * @author Apex Tunnel Team
 * @since 2025-10-28
 */
@Data
public class UserRoleUpdateDTO {
    
    /**
     * 目标用户名
     */
    @NotBlank(message = "目标用户不能为空")
    private String targetUsername;
    
    /**
     * 新角色（SUPER_ADMIN/ADMIN/USER）
     */
    @NotBlank(message = "新角色不能为空")
    private String newRole;
    
    /**
     * 备注（可选）
     */
    private String remark;
}

