package com.bytelab.tkline.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bytelab.tkline.server.dto.relation.NodeSubscriptionBindDTO;
import com.bytelab.tkline.server.dto.relation.RelationBindDTO;
import com.bytelab.tkline.server.entity.NodeSubscriptionRelation;
import com.bytelab.tkline.server.exception.BusinessException;
import com.bytelab.tkline.server.mapper.NodeMapper;
import com.bytelab.tkline.server.mapper.NodeSubscriptionRelationMapper;

import com.bytelab.tkline.server.service.NodeSubscriptionRelationService;
import com.bytelab.tkline.server.util.SubscriptionOrderGenerator;
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
    private final com.bytelab.tkline.server.mapper.SubscriptionMapper subscriptionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindNodeSubscriptions(RelationBindDTO bindDTO) {
        Long nodeId = bindDTO.getNodeId();
        List<Long> subscriptionIds = bindDTO.getSubscriptionIds();

        // 1. 检查节点是否存在
        if (nodeMapper.selectById(nodeId) == null) {
            throw new BusinessException("节点不存在: " + nodeId);
        }

        // 2. 查询已存在的绑定关系
        List<NodeSubscriptionRelation> existingRelations = relationMapper.selectList(
                new LambdaQueryWrapper<NodeSubscriptionRelation>()
                        .eq(NodeSubscriptionRelation::getNodeId, nodeId)
                        .eq(NodeSubscriptionRelation::getDeleted, 0));

        Set<Long> boundSubscriptionIds = existingRelations.stream()
                .map(NodeSubscriptionRelation::getSubscriptionId)
                .collect(Collectors.toSet());

        // 3. 过滤出需要新增的绑定
        List<NodeSubscriptionRelation> toInsert = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Long subId : subscriptionIds) {
            if (!boundSubscriptionIds.contains(subId)) {
                NodeSubscriptionRelation relation = new NodeSubscriptionRelation();
                relation.setNodeId(nodeId);
                relation.setSubscriptionId(subId);
                relation.setStatus(1); // 默认有效
                relation.setTrafficUsed(0L);
                relation.setCreateTime(now);
                relation.setCreateBy("admin"); // TODO: current user
                relation.setDeleted(0);
                toInsert.add(relation);
            }
        }

        // 4. 批量插入
        if (!toInsert.isEmpty()) {
            relationMapper.insertBatch(toInsert);
            log.info("Bound {} subscriptions to node {}", toInsert.size(), nodeId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchBindNodeSubscriptions(List<NodeSubscriptionBindDTO> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return;
        }

        List<NodeSubscriptionRelation> toInsert = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (NodeSubscriptionBindDTO binding : bindings) {
            // 验证有效期
            if (binding.getValidFrom() != null && binding.getValidTo() != null) {
                if (binding.getValidFrom().isAfter(binding.getValidTo())) {
                    throw new BusinessException("有效期开始时间不能晚于结束时间");
                }
            }

            // 生成子订单号(独立格式)
            String childOrderNo = SubscriptionOrderGenerator.generateChildOrderNo();

            NodeSubscriptionRelation relation = new NodeSubscriptionRelation();
            relation.setNodeId(binding.getNodeId());
            relation.setSubscriptionId(binding.getSubscriptionId());
            relation.setOrderNo(childOrderNo);
            relation.setValidFrom(binding.getValidFrom());
            relation.setValidTo(binding.getValidTo());
            relation.setTrafficLimit(binding.getTrafficLimit());
            relation.setTrafficUsed(0L);
            relation.setStatus(binding.getStatus() != null ? binding.getStatus() : 1); // 默认有效
            relation.setCreateTime(now);
            relation.setCreateBy("admin"); // TODO: current user
            relation.setDeleted(0);
            toInsert.add(relation);

            log.info("Generated child order: {} for node: {}, subscription: {}",
                    childOrderNo, binding.getNodeId(), binding.getSubscriptionId());
        }

        if (!toInsert.isEmpty()) {
            relationMapper.insertBatch(toInsert);
            log.info("Batch bound {} node-subscription relations", toInsert.size());
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
                relation.setStatus(1); // 默认有效
                relation.setTrafficUsed(0L);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncSubscriptionNodeBindings(Long subscriptionId, List<NodeSubscriptionBindDTO> bindings) {
        if (bindings == null) {
            bindings = new ArrayList<>();
        }

        // 1. 查询已存在的绑定关系
        List<NodeSubscriptionRelation> existingRelations = relationMapper.selectList(
                new LambdaQueryWrapper<NodeSubscriptionRelation>()
                        .eq(NodeSubscriptionRelation::getSubscriptionId, subscriptionId)
                        .eq(NodeSubscriptionRelation::getDeleted, 0));

        Set<Long> existingNodeIds = existingRelations.stream()
                .map(NodeSubscriptionRelation::getNodeId)
                .collect(Collectors.toSet());

        Set<Long> newNodeIds = bindings.stream()
                .map(NodeSubscriptionBindDTO::getNodeId)
                .collect(Collectors.toSet());

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

        // 3. 处理新增和更新
        LocalDateTime now = LocalDateTime.now();
        for (NodeSubscriptionBindDTO binding : bindings) {
            // 验证有效期
            if (binding.getValidFrom() != null && binding.getValidTo() != null) {
                if (binding.getValidFrom().isAfter(binding.getValidTo())) {
                    throw new BusinessException("有效期开始时间不能晚于结束时间");
                }
            }

            if (!existingNodeIds.contains(binding.getNodeId())) {
                // 新增
                NodeSubscriptionRelation relation = new NodeSubscriptionRelation();
                relation.setNodeId(binding.getNodeId());
                relation.setSubscriptionId(subscriptionId);
                relation.setValidFrom(binding.getValidFrom());
                relation.setValidTo(binding.getValidTo());
                relation.setTrafficLimit(binding.getTrafficLimit());
                relation.setTrafficUsed(0L);
                relation.setStatus(binding.getStatus() != null ? binding.getStatus() : 1);
                relation.setCreateTime(now);
                relation.setCreateBy("admin"); // TODO: current user
                relation.setDeleted(0);
                relationMapper.insert(relation);
            } else {
                // 更新已存在的关系
                NodeSubscriptionRelation existing = existingRelations.stream()
                        .filter(r -> r.getNodeId().equals(binding.getNodeId()))
                        .findFirst()
                        .orElse(null);

                if (existing != null) {
                    existing.setValidFrom(binding.getValidFrom());
                    existing.setValidTo(binding.getValidTo());
                    existing.setTrafficLimit(binding.getTrafficLimit());
                    existing.setStatus(binding.getStatus() != null ? binding.getStatus() : existing.getStatus());
                    existing.setUpdateTime(now);
                    existing.setUpdateBy("admin"); // TODO: current user
                    relationMapper.updateById(existing);
                }
            }
        }

        log.info("Synced {} node bindings for subscription {}", bindings.size(), subscriptionId);
    }
}
