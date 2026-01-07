package com.bytelab.tkline.server.converter;

import com.bytelab.tkline.server.dto.subscription.SubscriptionCreateDTO;
import com.bytelab.tkline.server.dto.subscription.SubscriptionDTO;
import com.bytelab.tkline.server.dto.subscription.SubscriptionUpdateDTO;
import com.bytelab.tkline.server.entity.Subscription;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubscriptionConverter {

    @Mapping(target = "statusLabel", expression = "java(com.bytelab.tkline.server.enums.SubscriptionStatus.getLabelByCode(entity.getStatus()))")
    @Mapping(target = "isValid", expression = "java(entity.getStatus() == 1 && (entity.getValidTo() == null || entity.getValidTo().isAfter(java.time.LocalDateTime.now())) && (entity.getTrafficLimit() == null || entity.getTrafficLimit() > (entity.getTrafficUsed() == null ? 0L : entity.getTrafficUsed())))")
    @Mapping(target = "isExpired", expression = "java(entity.getValidTo() != null && entity.getValidTo().isBefore(java.time.LocalDateTime.now()))")
    @Mapping(target = "isTrafficExhausted", expression = "java(entity.getTrafficLimit() != null && entity.getTrafficUsed() != null && entity.getTrafficUsed() >= entity.getTrafficLimit())")
    @Mapping(target = "nodeCount", ignore = true)
    SubscriptionDTO toDTO(Subscription entity);

    List<SubscriptionDTO> toDTOList(List<Subscription> list);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "1") // 默认有效
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "trafficUsed", constant = "0L")
    Subscription toEntity(SubscriptionCreateDTO dto);

    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "status", ignore = true) // 状态单独接口修改
    @Mapping(target = "trafficUsed", ignore = true)
    Subscription toEntity(SubscriptionUpdateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "trafficUsed", ignore = true)
    void updateEntityFromDto(SubscriptionUpdateDTO dto, @org.mapstruct.MappingTarget Subscription entity);

}
