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

    @Mapping(target = "nodeCount", ignore = true)
    SubscriptionDTO toDTO(Subscription entity);

    List<SubscriptionDTO> toDTOList(List<Subscription> list);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    Subscription toEntity(SubscriptionCreateDTO dto);

    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Subscription toEntity(SubscriptionUpdateDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntityFromDto(SubscriptionUpdateDTO dto, @org.mapstruct.MappingTarget Subscription entity);

}
