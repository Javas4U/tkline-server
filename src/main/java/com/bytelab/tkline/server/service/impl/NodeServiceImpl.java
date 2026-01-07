package com.bytelab.tkline.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.bytelab.tkline.server.converter.NodeConverter;
import com.bytelab.tkline.server.converter.SubscriptionConverter;
import com.bytelab.tkline.server.dto.node.NodeCreateDTO;
import com.bytelab.tkline.server.dto.node.NodeDTO;
import com.bytelab.tkline.server.dto.node.NodeHeartbeatDTO;
import com.bytelab.tkline.server.entity.Node;
import com.bytelab.tkline.server.entity.Subscription;
import com.bytelab.tkline.server.exception.BusinessException;
import com.bytelab.tkline.server.mapper.NodeMapper;
import com.bytelab.tkline.server.service.NodeService;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 节点服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NodeServiceImpl implements NodeService {

    private final NodeMapper nodeMapper;
    private final NodeConverter nodeConverter;

    private final SubscriptionConverter subscriptionConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createNode(NodeCreateDTO createDTO) {
        // 1. 检查名称是否存在
        System.out.println("Checking node name: " + createDTO.getName());
        boolean exists = nodeMapper.exists(new LambdaQueryWrapper<Node>()
                .eq(Node::getName, createDTO.getName()));
        if (exists) {
            throw new BusinessException("节点名称已存在: " + createDTO.getName());
        }

        // 2. 转换并保存
        Node node = nodeConverter.toEntity(createDTO);
        node.setCreateBy("admin"); // TODO: 获取当前登录用户
        node.setUpdateBy("admin");

        nodeMapper.insert(node);
        log.info("Node created: id={}, name={}", node.getId(), node.getName());

        return node.getId();
    }

    @Override
    public NodeDTO getNodeDetail(Long id) {
        Node node = nodeMapper.selectById(id);
        if (node == null) {
            throw new BusinessException("节点不存在: " + id);
        }
        return nodeConverter.toDTO(node);
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<NodeDTO> pageNodes(
            com.bytelab.tkline.server.dto.node.NodeQueryDTO query) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Node> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                query.getPage(), query.getPageSize());

        LambdaQueryWrapper<Node> wrapper = new LambdaQueryWrapper<>();
        if (query.getName() != null && !query.getName().isEmpty()) {
            wrapper.like(Node::getName, query.getName());
        }
        if (query.getIpAddress() != null && !query.getIpAddress().isEmpty()) {
            wrapper.eq(Node::getIpAddress, query.getIpAddress());
        }
        if (query.getRegion() != null && !query.getRegion().isEmpty()) {
            wrapper.eq(Node::getRegion, query.getRegion());
        }
        // ignore status for now if type mismatch or handle if needed

        wrapper.orderByDesc(Node::getId);

        com.baomidou.mybatisplus.core.metadata.IPage<Node> result = nodeMapper.selectPage(page, wrapper);
        return result.convert(nodeConverter::toDTO);
    }

    @Override
    public void heartbeat(NodeHeartbeatDTO heartbeatDTO) {
        Node node = nodeMapper.selectById(heartbeatDTO.getId());
        if (node == null) {
            throw new RuntimeException("节点不存在");
        }
        node.setLastHeartbeatTime(LocalDateTime.now());
        nodeMapper.updateById(node);
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<com.bytelab.tkline.server.dto.subscription.SubscriptionDTO> pageNodeSubscriptions(
            Long nodeId, com.bytelab.tkline.server.dto.PageQueryDTO query) {
        // 创建分页对象
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Subscription> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                query.getPage(), query.getPageSize());

        // 执行查询
        com.baomidou.mybatisplus.core.metadata.IPage<Subscription> result = nodeMapper.selectSubscriptionsByNodeId(page,
                nodeId);

        // 转换结果
        return result.convert(subscriptionConverter::toDTO);
    }
}
