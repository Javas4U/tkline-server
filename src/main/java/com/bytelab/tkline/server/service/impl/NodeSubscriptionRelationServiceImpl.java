package com.bytelab.tkline.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bytelab.tkline.server.dto.relation.RelationBindDTO;
import com.bytelab.tkline.server.entity.NodeSubscriptionRelation;
import com.bytelab.tkline.server.exception.BusinessException;
import com.bytelab.tkline.server.mapper.NodeMapper;
import com.bytelab.tkline.server.mapper.NodeSubscriptionRelationMapper;

import com.bytelab.tkline.server.service.NodeSubscriptionRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 节点订阅关联服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NodeSubscriptionRelationServiceImpl implements NodeSubscriptionRelationService {

    private final NodeSubscriptionRelationMapper relationMapper;
    private final NodeMapper nodeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindNodeSubscriptions(RelationBindDTO bindDTO) {
        Long nodeId = bindDTO.getNodeId();
        List<Long> subscriptionIds = bindDTO.getSubscriptionIds();

        // 1. 检查节点是否存在
        if (nodeMapper.selectById(nodeId) == null) {
            throw new BusinessException("节点不存在: " + nodeId);
        }

        // 2. 检查订阅是否存在 (可选，这里假设前端传来的都是存在的)
        // 也可以查询一次 count 是否匹配

        // 3. 查询已存在的绑定关系
        List<NodeSubscriptionRelation> existingRelations = relationMapper.selectList(
                new LambdaQueryWrapper<NodeSubscriptionRelation>()
                        .eq(NodeSubscriptionRelation::getNodeId, nodeId)
                        .eq(NodeSubscriptionRelation::getDeleted, 0));

        Set<Long> boundSubscriptionIds = existingRelations.stream()
                .map(NodeSubscriptionRelation::getSubscriptionId)
                .collect(Collectors.toSet());

        // 4. 过滤出需要新增的绑定
        List<NodeSubscriptionRelation> toInsert = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Long subId : subscriptionIds) {
            if (!boundSubscriptionIds.contains(subId)) {
                NodeSubscriptionRelation relation = new NodeSubscriptionRelation();
                relation.setNodeId(nodeId);
                relation.setSubscriptionId(subId);
                relation.setCreateTime(now);
                relation.setCreateBy("admin"); // TODO: current user
                relation.setDeleted(0);
                toInsert.add(relation);
            }
        }

        // 5. 批量插入
        if (!toInsert.isEmpty()) {
            relationMapper.insertBatch(toInsert);
            log.info("Bound {} subscriptions to node {}", toInsert.size(), nodeId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncSubscriptionNodes(Long subscriptionId, List<Long> nodeIds) {
        if (nodeIds == null) {
            nodeIds = new ArrayList<>();
        }

        // 1. 查询已存在的绑定关系
        List<NodeSubscriptionRelation> existingRelations = relationMapper.selectList(
                new LambdaQueryWrapper<NodeSubscriptionRelation>()
                        .eq(NodeSubscriptionRelation::getSubscriptionId, subscriptionId)
                        .eq(NodeSubscriptionRelation::getDeleted, 0));

        Set<Long> existingNodeIds = existingRelations.stream()
                .map(NodeSubscriptionRelation::getNodeId)
                .collect(Collectors.toSet());

        Set<Long> newNodeIds = new java.util.HashSet<>(nodeIds);

        // 2. 找出需要删除的关系
        List<Long> idsToDelete = existingRelations.stream()
                .filter(r -> !newNodeIds.contains(r.getNodeId()))
                .map(NodeSubscriptionRelation::getId)
                .collect(Collectors.toList());

        if (!idsToDelete.isEmpty()) {
            for (Long id : idsToDelete) {
                NodeSubscriptionRelation relation = new NodeSubscriptionRelation();
                relation.setId(id);
                relation.setDeleted(1);
                relation.setUpdateTime(LocalDateTime.now());
                relation.setUpdateBy("admin"); // TODO: current user
                relationMapper.updateById(relation);
            }
            log.info("Deleted {} node relations for subscription {}", idsToDelete.size(), subscriptionId);
        }

        // 3. 找出需要新增的关系
        List<NodeSubscriptionRelation> toInsert = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Long nodeId : nodeIds) {
            if (!existingNodeIds.contains(nodeId)) {
                NodeSubscriptionRelation relation = new NodeSubscriptionRelation();
                relation.setNodeId(nodeId);
                relation.setSubscriptionId(subscriptionId);
                relation.setCreateTime(now);
                relation.setCreateBy("admin"); // TODO: current user
                relation.setDeleted(0);
                toInsert.add(relation);
            }
        }

        // 4. 批量插入
        if (!toInsert.isEmpty()) {
            relationMapper.insertBatch(toInsert);
            log.info("Added {} node relations for subscription {}", toInsert.size(), subscriptionId);
        }
    }
}
