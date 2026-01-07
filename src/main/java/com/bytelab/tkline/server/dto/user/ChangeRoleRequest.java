package com.bytelab.tkline.server.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 修改用户角色请求DTO
 */
@Data
@Schema(description = "修改用户角色请求")
public class ChangeRoleRequest {

    @Schema(description = "用户名", example = "testuser", required = true)
    private String username;

    @Schema(description = "新角色 (SUPER_ADMIN, ADMIN, USER)", example = "ADMIN", required = true)
    private String newRole;

    @Schema(description = "备注", example = "Promotion")
    private String remark;
}
