package com.bytelab.tkline.server.converter;

import com.bytelab.tkline.server.dto.profile.ProfileInfoDTO;
import com.bytelab.tkline.server.entity.SysUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 个人中心数据转换器
 */
@Mapper(componentModel = "spring")
public interface ProfileConverter {

    ProfileConverter INSTANCE = Mappers.getMapper(ProfileConverter.class);

    /**
     * 用户Entity转个人中心DTO
     */
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "registrationTime", source = "createTime")
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "subscriptionType", ignore = true)
    @Mapping(target = "subscriptionExpireTime", ignore = true)
    @Mapping(target = "inviteCode", ignore = true)
    @Mapping(target = "totalTraffic", ignore = true)
    @Mapping(target = "monthlyTrafficLimit", ignore = true)
    @Mapping(target = "maxDevices", ignore = true)
    @Mapping(target = "deviceCount", ignore = true)
    @Mapping(target = "inviteCount", ignore = true)
    @Mapping(target = "totalCommission", ignore = true)
    @Mapping(target = "notificationSettings", ignore = true)
    @Mapping(target = "securitySettings", ignore = true)
    ProfileInfoDTO toProfileInfoDTO(SysUser sysUser);
}
