package com.bytelab.tkline.server.converter;

import com.bytelab.tkline.server.dto.node.NodeCreateDTO;
import com.bytelab.tkline.server.dto.node.NodeDTO;
import com.bytelab.tkline.server.dto.node.NodeUpdateDTO;
import com.bytelab.tkline.server.entity.Node;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface NodeConverter {

    @Mapping(target = "statusLabel", expression = "java(com.bytelab.tkline.server.enums.NodeStatus.getLabelByCode(entity.getStatus()))")
    @Mapping(target = "online", source = "lastHeartbeatTime", qualifiedByName = "isOnlineFromHeartbeat")
    @Mapping(target = "subscriptionCount", ignore = true)
    NodeDTO toDTO(Node entity);

    List<NodeDTO> toDTOList(List<Node> list);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "1") // 默认在线
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "lastHeartbeatTime", ignore = true)
    Node toEntity(NodeCreateDTO dto);

    @Mapping(target = "status", ignore = true) // 状态由业务逻辑单独处理
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "lastHeartbeatTime", ignore = true)
    Node toEntity(NodeUpdateDTO dto);

    // 辅助方法：将 status (0=离线, 1=在线) 转换为 Boolean
    @org.mapstruct.Named("statusToOnline")
    default Boolean statusToOnline(Integer status) {
        return status != null && status == 1;
    }

    // 辅助方法：根据最后心跳时间判断节点是否在线（5分钟内有心跳即为在线）
    @org.mapstruct.Named("isOnlineFromHeartbeat")
    default Boolean isOnlineFromHeartbeat(LocalDateTime lastHeartbeatTime) {
        if (lastHeartbeatTime == null) {
            return false;
        }
        // 如果最后心跳时间在5分钟内，认为节点在线
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusMinutes(5);
        return lastHeartbeatTime.isAfter(threshold);
    }

}
