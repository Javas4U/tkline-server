package com.bytelab.tkline.server.converter;

import com.bytelab.tkline.server.dto.relation.NodeSubscriptionBindDTO;
import com.bytelab.tkline.server.dto.relation.NodeSubscriptionRelationDTO;
import com.bytelab.tkline.server.entity.NodeSubscriptionRelation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NodeSubscriptionRelationConverter {

    @Mapping(target = "statusLabel", expression = "java(com.bytelab.tkline.server.enums.SubscriptionStatus.getLabelByCode(entity.getStatus()))")
    @Mapping(target = "isValid", expression = "java(entity.getStatus() != null && entity.getStatus() == 1 && (entity.getValidTo() == null || entity.getValidTo().isAfter(java.time.LocalDateTime.now())) && (entity.getTrafficLimit() == null || entity.getTrafficLimit() > (entity.getTrafficUsed() == null ? 0L : entity.getTrafficUsed())))")
    @Mapping(target = "isExpired", expression = "java(entity.getValidTo() != null && entity.getValidTo().isBefore(java.time.LocalDateTime.now()))")
    @Mapping(target = "isTrafficExhausted", expression = "java(entity.getTrafficLimit() != null && entity.getTrafficUsed() != null && entity.getTrafficUsed() >= entity.getTrafficLimit())")
    @Mapping(target = "nodeName", ignore = true)
    @Mapping(target = "subscriptionName", ignore = true)
    NodeSubscriptionRelationDTO toDTO(NodeSubscriptionRelation entity);

    List<NodeSubscriptionRelationDTO> toDTOList(List<NodeSubscriptionRelation> list);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trafficUsed", constant = "0L")
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    NodeSubscriptionRelation toEntity(NodeSubscriptionBindDTO dto);
}
