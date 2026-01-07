package com.bytelab.tkline.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bytelab.tkline.server.converter.NodeConverter;
import com.bytelab.tkline.server.converter.SubscriptionConverter;
import com.bytelab.tkline.server.dto.subscription.SubscriptionCreateDTO;
import com.bytelab.tkline.server.dto.subscription.SubscriptionDTO;
import com.bytelab.tkline.server.entity.Subscription;
import com.bytelab.tkline.server.exception.BusinessException;
import com.bytelab.tkline.server.mapper.SubscriptionMapper;
import com.bytelab.tkline.server.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订阅服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionConverter subscriptionConverter;
    private final NodeConverter nodeConverter;
    private final com.bytelab.tkline.server.service.NodeSubscriptionRelationService nodeSubscriptionRelationService;
    private final com.bytelab.tkline.server.mapper.NodeSubscriptionRelationMapper nodeSubscriptionRelationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSubscription(SubscriptionCreateDTO createDTO) {
        // 1. 验证有效期
        if (createDTO.getValidFrom().isAfter(createDTO.getValidTo())) {
            throw new BusinessException("有效期开始时间不能晚于结束时间");
        }

        // 2. 检查名称是否存在
        boolean exists = subscriptionMapper.exists(new LambdaQueryWrapper<Subscription>()
                .eq(Subscription::getName, createDTO.getName()));
        if (exists) {
            throw new BusinessException("订阅名称已存在: " + createDTO.getName());
        }

        // 3. 转换并保存
        Subscription subscription = subscriptionConverter.toEntity(createDTO);
        subscription.setCreateBy("admin"); // TODO: 获取当前登录用户
        subscription.setUpdateBy("admin");

        subscriptionMapper.insert(subscription);
        log.info("Subscription created: id={}, name={}", subscription.getId(), subscription.getName());

        // 4. 绑定节点（如果提供了节点ID列表）
        if (createDTO.getNodeIds() != null) {
            nodeSubscriptionRelationService.syncSubscriptionNodes(subscription.getId(), createDTO.getNodeIds());
        }

        return subscription.getId();
    }

    @Override
    public SubscriptionDTO getSubscriptionDetail(Long id) {
        Subscription subscription = subscriptionMapper.selectById(id);
        if (subscription == null) {
            throw new BusinessException("订阅不存在: " + id);
        }
        return subscriptionConverter.toDTO(subscription);
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<SubscriptionDTO> pageSubscriptions(
            com.bytelab.tkline.server.dto.subscription.SubscriptionQueryDTO query) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Subscription> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                query.getPage(), query.getPageSize());

        LambdaQueryWrapper<Subscription> wrapper = new LambdaQueryWrapper<>();
        if (query.getName() != null && !query.getName().isEmpty()) {
            wrapper.like(Subscription::getName, query.getName());
        }
        if (query.getType() != null && !query.getType().isEmpty()) {
            wrapper.eq(Subscription::getType, query.getType());
        }

        wrapper.orderByDesc(Subscription::getId);

        com.baomidou.mybatisplus.core.metadata.IPage<Subscription> result = subscriptionMapper.selectPage(page,
                wrapper);
        return result.convert(subscriptionConverter::toDTO);
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<com.bytelab.tkline.server.dto.node.NodeDTO> pageSubscriptionNodes(
            Long subscriptionId, com.bytelab.tkline.server.dto.PageQueryDTO query) {
        // 创建分页对象
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.bytelab.tkline.server.entity.Node> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                query.getPage(), query.getPageSize());

        // 执行查询
        com.baomidou.mybatisplus.core.metadata.IPage<com.bytelab.tkline.server.entity.Node> result = subscriptionMapper
                .selectNodesBySubscriptionId(page, subscriptionId);

        // 转换结果
        return result.convert(nodeConverter::toDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSubscription(com.bytelab.tkline.server.dto.subscription.SubscriptionUpdateDTO updateDTO) {
        // 1. 检查是否存在
        Subscription existing = subscriptionMapper.selectById(updateDTO.getId());
        if (existing == null) {
            throw new BusinessException("订阅不存在: " + updateDTO.getId());
        }

        // 2. 验证有效期
        if (updateDTO.getValidFrom() != null && updateDTO.getValidTo() != null) {
            if (updateDTO.getValidFrom().isAfter(updateDTO.getValidTo())) {
                throw new BusinessException("有效期开始时间不能晚于结束时间");
            }
        }

        // 3. 转换并更新
        subscriptionConverter.updateEntityFromDto(updateDTO, existing);
        existing.setUpdateBy("admin"); // TODO: current user

        int rows = subscriptionMapper.updateById(existing);
        if (rows == 0) {
            throw new BusinessException("更新失败，可能已被其他用户修改");
        }

        // 4. 同步节点绑定
        if (updateDTO.getNodeIds() != null) {
            nodeSubscriptionRelationService.syncSubscriptionNodes(existing.getId(), updateDTO.getNodeIds());
        }

        log.info("Subscription updated: id={}, name={}", existing.getId(), existing.getName());
    }

    @Override
    public java.util.List<Long> getSubscriptionNodeIds(Long subscriptionId) {
        return nodeSubscriptionRelationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.bytelab.tkline.server.entity.NodeSubscriptionRelation>()
                        .eq(com.bytelab.tkline.server.entity.NodeSubscriptionRelation::getSubscriptionId,
                                subscriptionId)
                        .eq(com.bytelab.tkline.server.entity.NodeSubscriptionRelation::getDeleted, 0))
                .stream()
                .map(com.bytelab.tkline.server.entity.NodeSubscriptionRelation::getNodeId)
                .collect(java.util.stream.Collectors.toList());
    }
}
