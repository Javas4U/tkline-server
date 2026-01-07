package com.bytelab.tkline.server.dto.user;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * 用户详情DTO
 * 
 * @author Apex Tunnel Team
 * @since 2025-10-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDetailDTO extends UserManagementDTO {
    
    /**
     * 角色变更历史
     */
    private List<RoleChangeLogDTO> roleChangeHistory;
}

