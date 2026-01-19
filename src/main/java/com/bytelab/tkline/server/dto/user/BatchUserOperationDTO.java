package com.bytelab.tkline.server.dto.user;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 批量用户操作DTO
 * 
 * @author Apex Tunnel Team
 * @since 2025-10-28
 */
@Data
public class BatchUserOperationDTO {
    
    /**
     * 用户名列表（最多100个）
     */
    @NotEmpty(message = "用户名列表不能为空")
    @Size(max = 100, message = "批量操作最多支持100个用户")
    private List<String> usernames;
    
    /**
     * 新角色（SUPER_ADMIN/ADMIN/USER）
     */
    @NotBlank(message = "新角色不能为空")
    private String newRole;
}

