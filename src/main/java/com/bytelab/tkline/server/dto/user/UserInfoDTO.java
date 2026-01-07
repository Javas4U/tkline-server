package com.bytelab.tkline.server.dto.user;

import com.bytelab.tkline.server.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户信息DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserInfoDTO extends BaseDTO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String role; // 用户角色（SUPER_ADMIN/ADMIN/USER）
    private Integer status; // 状态：0-禁用，1-启用
}
