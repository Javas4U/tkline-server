package com.bytelab.tkline.server.converter;

import com.bytelab.tkline.server.dto.user.CreateUserRequest;
import com.bytelab.tkline.server.dto.user.UserInfoDTO;
import com.bytelab.tkline.server.dto.user.UserManagementDTO;
import com.bytelab.tkline.server.entity.SysUser;
import com.bytelab.tkline.server.enums.UserRole;
import com.bytelab.tkline.server.enums.UserStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * 用户实体与DTO转换器
 */
@Mapper(componentModel = "spring")
public interface UserConverter {

    /**
     * Entity转DTO
     */
    UserInfoDTO toUserInfoDTO(SysUser sysUser);

    /**
     * Entity列表转DTO列表
     */
    List<UserInfoDTO> toUserInfoDTOList(List<SysUser> sysUsers);

    /**
     * 创建请求转Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "1")
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", constant = "0")
    @Mapping(target = "role", ignore = true)
    SysUser toEntity(CreateUserRequest request);
    
    /**
     * Entity转用户管理DTO（带角色和状态显示名称）
     */
    @Mapping(target = "roleDisplay", expression = "java(userRoleDisplay(sysUser.getRole()))")
    UserManagementDTO toUserManagementDTO(SysUser sysUser);
    
    /**
     * Entity列表转用户管理DTO列表
     */
    List<UserManagementDTO> toUserManagementDTOList(List<SysUser> sysUsers);
    
    /**
     * 获取角色显示名称
     */
    @Named("userRoleDisplay")
    default String userRoleDisplay(String roleCode) {
        if (roleCode == null) {
            return null;
        }
        try {
            return UserRole.fromCode(roleCode).getDescription();
        } catch (IllegalArgumentException e) {
            return roleCode;
        }
    }
    
    /**
     * 获取状态显示名称
     */
    @Named("userStatusDisplay")
    default String userStatusDisplay(String statusCode) {
        if (statusCode == null) {
            return null;
        }
        try {
            return UserStatus.fromCode(statusCode).getDescription();
        } catch (IllegalArgumentException e) {
            return statusCode;
        }
    }
}
