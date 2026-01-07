package com.bytelab.tkline.server.dto.user;

import com.bytelab.tkline.server.dto.PageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询DTO
 * 
 * @author Apex Tunnel Team
 * @since 2025-10-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends PageQueryDTO {
    
    /**
     * 搜索关键词（用户名或邮箱）
     */
    private String keyword;
    
    /**
     * 角色筛选（SUPER_ADMIN/ADMIN/USER）
     */
    private String role;
    
    /**
     * 状态筛选（NORMAL/BLACKLISTED）
     */
    private String accountStatus;
}

