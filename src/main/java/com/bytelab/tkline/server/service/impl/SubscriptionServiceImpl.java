package com.bytelab.tkline.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bytelab.tkline.server.converter.SubscriptionConverter;
import com.bytelab.tkline.server.dto.PageQueryDTO;
import com.bytelab.tkline.server.dto.node.NodeDTO;
import com.bytelab.tkline.server.dto.relation.NodeSubscriptionBindDTO;
import com.bytelab.tkline.server.dto.subscription.SubscriptionCreateDTO;
import com.bytelab.tkline.server.dto.subscription.SubscriptionDTO;
import com.bytelab.tkline.server.dto.subscription.SubscriptionQueryDTO;
import com.bytelab.tkline.server.dto.subscription.SubscriptionUpdateDTO;
import com.bytelab.tkline.server.entity.Node;
import com.bytelab.tkline.server.entity.NodeSubscriptionRelation;
import com.bytelab.tkline.server.entity.Subscription;
import com.bytelab.tkline.server.exception.BusinessException;
import com.bytelab.tkline.server.mapper.NodeMapper;
import com.bytelab.tkline.server.mapper.NodeSubscriptionRelationMapper;
import com.bytelab.tkline.server.mapper.SubscriptionMapper;
import com.bytelab.tkline.server.service.NodeSubscriptionRelationService;
import com.bytelab.tkline.server.service.SubscriptionService;
import com.bytelab.tkline.server.util.SubscriptionOrderGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 订阅服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionConverter subscriptionConverter;
    private final NodeSubscriptionRelationService nodeSubscriptionRelationService;
    private final NodeSubscriptionRelationMapper nodeSubscriptionRelationMapper;
    private final NodeMapper nodeMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSubscription(SubscriptionCreateDTO createDTO) {
        // 1. 检查名称是否存在
        boolean exists = subscriptionMapper.exists(new LambdaQueryWrapper<Subscription>()
                .eq(Subscription::getGroupName, createDTO.getGroupName()));
        if (exists) {
            throw new BusinessException("订阅组名称已存在: " + createDTO.getGroupName());
        }

        // 2. 转换并保存订阅基本信息
        Subscription subscription = subscriptionConverter.toEntity(createDTO);
        subscription.setCreateBy("admin"); // TODO: 获取当前登录用户
        subscription.setUpdateBy("admin");

        // 如果没有提供orderNo，则使用雪花算法生成
        if (subscription.getOrderNo() == null || subscription.getOrderNo().isEmpty()) {
            subscription.setOrderNo(SubscriptionOrderGenerator.generateOrderNo());
        }

        subscriptionMapper.insert(subscription);
        log.info("Subscription created: id={}, groupName={}, orderNo={}",
                subscription.getId(), subscription.getGroupName(), subscription.getOrderNo());

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
    public IPage<SubscriptionDTO> pageSubscriptions(
            SubscriptionQueryDTO query) {
        Page<Subscription> page = new Page<>(
                query.getPage(), query.getPageSize());

        LambdaQueryWrapper<Subscription> wrapper = new LambdaQueryWrapper<>();
        if (query.getGroupName() != null && !query.getGroupName().isEmpty()) {
            wrapper.like(Subscription::getGroupName, query.getGroupName());
        }
        if (query.getOrderNo() != null && !query.getOrderNo().isEmpty()) {
            wrapper.like(Subscription::getOrderNo, query.getOrderNo());
        }

        wrapper.orderByDesc(Subscription::getId);

        IPage<Subscription> result = subscriptionMapper.selectPage(page,
                wrapper);

        // 转换为DTO并计算节点数
        IPage<SubscriptionDTO> dtoPage = result.convert(subscription -> {
            SubscriptionDTO dto = subscriptionConverter.toDTO(subscription);

            // 计算总节点数
            Long totalNodeCount = nodeSubscriptionRelationMapper.selectCount(
                new LambdaQueryWrapper<NodeSubscriptionRelation>()
                    .eq(NodeSubscriptionRelation::getSubscriptionId, subscription.getId())
                    .eq(NodeSubscriptionRelation::getDeleted, 0)
            );
            dto.setNodeCount(totalNodeCount.intValue());

            // 计算可用节点数(状态为1=有效)
            Long availableNodeCount = nodeSubscriptionRelationMapper.selectCount(
                new LambdaQueryWrapper<NodeSubscriptionRelation>()
                    .eq(NodeSubscriptionRelation::getSubscriptionId, subscription.getId())
                    .eq(NodeSubscriptionRelation::getStatus, 1)
                    .eq(NodeSubscriptionRelation::getDeleted, 0)
            );
            dto.setAvailableNodeCount(availableNodeCount.intValue());

            return dto;
        });

        return dtoPage;
    }

    @Override
    public IPage<NodeDTO> pageSubscriptionNodes(
            Long subscriptionId, PageQueryDTO query) {
        // 创建分页对象
        Page<NodeDTO> page =
                new Page<>(query.getPage(), query.getPageSize());

        // 执行查询 - 直接返回包含绑定配置的 NodeDTO,支持名称和区域查询
        return subscriptionMapper.selectNodesBySubscriptionId(page, subscriptionId, query.getName(), query.getRegion());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSubscription(SubscriptionUpdateDTO updateDTO) {
        // 1. 检查是否存在
        Subscription existing = subscriptionMapper.selectById(updateDTO.getId());
        if (existing == null) {
            throw new BusinessException("订阅不存在: " + updateDTO.getId());
        }

        // 2. 转换并更新
        subscriptionConverter.updateEntityFromDto(updateDTO, existing);
        existing.setUpdateBy("admin"); // TODO: current user

        int rows = subscriptionMapper.updateById(existing);
        if (rows == 0) {
            throw new BusinessException("更新失败，可能已被其他用户修改");
        }

        log.info("Subscription updated: id={}, groupName={}", existing.getId(), existing.getGroupName());
    }

    @Override
    public java.util.List<Long> getSubscriptionNodeIds(Long subscriptionId) {
        return nodeSubscriptionRelationMapper.selectList(
                new LambdaQueryWrapper<NodeSubscriptionRelation>()
                        .eq(NodeSubscriptionRelation::getSubscriptionId,
                                subscriptionId)
                        .eq(NodeSubscriptionRelation::getDeleted, 0))
                .stream()
                .map(NodeSubscriptionRelation::getNodeId)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindNodesWithConfig(Long subscriptionId, java.util.List<NodeSubscriptionBindDTO> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return;
        }

        // 设置订阅ID
        for (NodeSubscriptionBindDTO binding : bindings) {
            binding.setSubscriptionId(subscriptionId);
        }

        // 批量绑定节点
        nodeSubscriptionRelationService.batchBindNodeSubscriptions(bindings);
        log.info("Bound {} nodes to subscription: subscriptionId={}", bindings.size(), subscriptionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindNodes(Long subscriptionId, java.util.List<Long> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) {
            return;
        }

        // 查询要删除的关联记录
        java.util.List<NodeSubscriptionRelation> relations = nodeSubscriptionRelationMapper.selectList(
                new LambdaQueryWrapper<NodeSubscriptionRelation>()
                        .eq(NodeSubscriptionRelation::getSubscriptionId, subscriptionId)
                        .in(NodeSubscriptionRelation::getNodeId, nodeIds)
                        .eq(NodeSubscriptionRelation::getDeleted, 0));

        if (!relations.isEmpty()) {
            // 获取要删除的ID列表
            java.util.List<Long> relationIds = relations.stream()
                    .map(NodeSubscriptionRelation::getId)
                    .collect(java.util.stream.Collectors.toList());

            // 批量逻辑删除
            nodeSubscriptionRelationMapper.deleteBatch(relationIds);
            log.info("Unbound {} nodes from subscription: subscriptionId={}", relationIds.size(), subscriptionId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncNodeBindings(Long subscriptionId, java.util.List<NodeSubscriptionBindDTO> bindings) {
        nodeSubscriptionRelationService.syncSubscriptionNodeBindings(subscriptionId, bindings);
        log.info("Synced node bindings for subscription: subscriptionId={}", subscriptionId);
    }

    @Override
    public String getSubscriptionConfigUrl(Long subscriptionId, java.util.List<Long> nodeIds, String baseUrl) {
        // 获取订阅信息
        Subscription subscription = subscriptionMapper.selectById(subscriptionId);
        if (subscription == null) {
            throw new BusinessException("订阅不存在: " + subscriptionId);
        }

        String orderNo = subscription.getOrderNo();

        if (orderNo == null || orderNo.isEmpty()) {
            throw new BusinessException("订阅订单号未生成: " + subscriptionId);
        }

        // 构建订阅配置URL
        StringBuilder url = new StringBuilder(baseUrl);
        url.append("/api/subscription/config");
        url.append("?orderNo=").append(orderNo);

        // 如果指定了节点ID,添加nodeIds参数
        if (nodeIds != null && !nodeIds.isEmpty()) {
            String nodeIdsStr = nodeIds.stream()
                    .map(String::valueOf)
                    .collect(java.util.stream.Collectors.joining(","));
            url.append("&nodeIds=").append(nodeIdsStr);
        }

        log.info("Generated subscription config URL: subscriptionId={}, url={}", subscriptionId, url);
        return url.toString();
    }

    // ========== 订阅配置生成方法 ==========

    @Override
    public String generateYamlConfig(String orderNo, List<Long> nodeIds, String baseUrl) {
        log.info("生成YAML配置: orderNo={}, nodeIds={}", orderNo, nodeIds);

        // 1. 查询订阅信息
        Subscription subscription = subscriptionMapper.selectOne(
                new LambdaQueryWrapper<Subscription>()
                        .eq(Subscription::getOrderNo, orderNo)
        );

        if (subscription == null) {
            throw new BusinessException("订阅不存在: " + orderNo);
        }

        // 2. 查询订阅关联的节点
        List<Node> nodes = getSubscriptionNodesForConfig(subscription.getId(), nodeIds);

        if (nodes.isEmpty()) {
            throw new BusinessException("订阅暂无可用节点");
        }

        // 3. 生成Clash YAML配置
        return buildClashYaml(subscription, nodes, baseUrl);
    }

    @Override
    public String generateJsonConfig(String orderNo, List<Long> nodeIds, String baseUrl) {
        log.info("生成JSON配置: orderNo={}, nodeIds={}", orderNo, nodeIds);

        // 1. 查询订阅信息
        Subscription subscription = subscriptionMapper.selectOne(
                new LambdaQueryWrapper<Subscription>()
                        .eq(Subscription::getOrderNo, orderNo)
        );

        if (subscription == null) {
            throw new BusinessException("订阅不存在: " + orderNo);
        }

        // 2. 查询订阅关联的节点
        List<Node> nodes = getSubscriptionNodesForConfig(subscription.getId(), nodeIds);

        if (nodes.isEmpty()) {
            throw new BusinessException("订阅暂无可用节点");
        }

        // 3. 生成Sing-Box JSON配置
        return buildSingBoxJson(subscription, nodes, baseUrl);
    }

    /**
     * 获取订阅关联的节点列表(用于配置生成)
     *
     * @param subscriptionId 订阅ID
     * @param filterNodeIds 可选的节点ID过滤列表,为null时返回所有节点
     * @return 节点列表
     */
    private List<Node> getSubscriptionNodesForConfig(Long subscriptionId, List<Long> filterNodeIds) {
        // 查询订阅关联的节点ID
        List<NodeSubscriptionRelation> relations = nodeSubscriptionRelationMapper.selectList(
                new LambdaQueryWrapper<NodeSubscriptionRelation>()
                        .eq(NodeSubscriptionRelation::getSubscriptionId, subscriptionId)
        );

        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取所有关联的节点ID
        List<Long> allNodeIds = relations.stream()
                .map(NodeSubscriptionRelation::getNodeId)
                .collect(Collectors.toList());

        // 如果指定了过滤节点ID,则只返回指定的节点
        List<Long> targetNodeIds;
        if (filterNodeIds != null && !filterNodeIds.isEmpty()) {
            // 取交集:只返回既在订阅中又在过滤列表中的节点
            targetNodeIds = allNodeIds.stream()
                    .filter(filterNodeIds::contains)
                    .collect(Collectors.toList());

            if (targetNodeIds.isEmpty()) {
                log.warn("指定的节点ID不在订阅中: subscriptionId={}, filterNodeIds={}",
                        subscriptionId, filterNodeIds);
                return Collections.emptyList();
            }
        } else {
            targetNodeIds = allNodeIds;
        }

        // 查询节点详情
        return nodeMapper.selectBatchIds(targetNodeIds);
    }

    /**
     * 构建Clash YAML配置
     */
    private String buildClashYaml(Subscription subscription, List<Node> nodes, String baseUrl) {
        StringBuilder yaml = new StringBuilder();

        // 基本配置
        yaml.append("# Clash配置文件\n");
        yaml.append("# 订阅组: ").append(subscription.getGroupName()).append("\n");
        yaml.append("# 订阅编号: ").append(subscription.getOrderNo()).append("\n");
        yaml.append("\n");
        yaml.append("port: 7890\n");
        yaml.append("socks-port: 7891\n");
        yaml.append("allow-lan: false\n");
        yaml.append("mode: rule\n");
        yaml.append("log-level: info\n");
        yaml.append("external-controller: 127.0.0.1:9090\n");
        yaml.append("\n");

        // 代理节点
        yaml.append("proxies:\n");
        for (Node node : nodes) {
            yaml.append("  - name: \"").append(node.getName()).append("\"\n");
            yaml.append("    type: hysteria2\n");
            yaml.append("    server: ").append(node.getIpAddress()).append("\n");
            yaml.append("    port: ").append(node.getPort()).append("\n");
            yaml.append("    password: your-password\n"); // 实际应从配置或节点信息中获取
            yaml.append("    sni: ").append(node.getIpAddress()).append("\n");
            yaml.append("    skip-cert-verify: false\n");
            yaml.append("\n");
        }

        // 代理组
        yaml.append("proxy-groups:\n");
        yaml.append("  - name: PROXY\n");
        yaml.append("    type: select\n");
        yaml.append("    proxies:\n");
        for (Node node : nodes) {
            yaml.append("      - \"").append(node.getName()).append("\"\n");
        }
        yaml.append("\n");

        // 规则
        yaml.append("rules:\n");
        yaml.append("  - DOMAIN-SUFFIX,google.com,PROXY\n");
        yaml.append("  - DOMAIN-KEYWORD,google,PROXY\n");
        yaml.append("  - GEOIP,CN,DIRECT\n");
        yaml.append("  - MATCH,PROXY\n");

        return yaml.toString();
    }

    /**
     * 构建Sing-Box JSON配置
     */
    private String buildSingBoxJson(Subscription subscription, List<Node> nodes, String baseUrl) {
        try {
            Map<String, Object> config = new LinkedHashMap<>();

            // 日志配置
            Map<String, Object> log = new LinkedHashMap<>();
            log.put("level", "info");
            log.put("timestamp", true);
            config.put("log", log);

            // 入站配置
            List<Map<String, Object>> inbounds = new ArrayList<>();

            // Mixed入站(HTTP+SOCKS5)
            Map<String, Object> mixed = new LinkedHashMap<>();
            mixed.put("type", "mixed");
            mixed.put("tag", "mixed-in");
            mixed.put("listen", "127.0.0.1");
            mixed.put("listen_port", 7890);
            inbounds.add(mixed);

            config.put("inbounds", inbounds);

            // 出站配置
            List<Map<String, Object>> outbounds = new ArrayList<>();

            // 添加节点出站
            for (Node node : nodes) {
                Map<String, Object> outbound = new LinkedHashMap<>();
                outbound.put("type", "hysteria2");
                outbound.put("tag", node.getName());
                outbound.put("server", node.getIpAddress());
                outbound.put("server_port", node.getPort());
                outbound.put("password", "your-password"); // 实际应从配置或节点信息中获取

                Map<String, Object> tls = new LinkedHashMap<>();
                tls.put("enabled", true);
                tls.put("server_name", node.getIpAddress());
                tls.put("insecure", false);
                outbound.put("tls", tls);

                outbounds.add(outbound);
            }

            // 添加direct出站
            Map<String, Object> direct = new LinkedHashMap<>();
            direct.put("type", "direct");
            direct.put("tag", "direct");
            outbounds.add(direct);

            // 添加block出站
            Map<String, Object> block = new LinkedHashMap<>();
            block.put("type", "block");
            block.put("tag", "block");
            outbounds.add(block);

            config.put("outbounds", outbounds);

            // 路由规则
            Map<String, Object> route = new LinkedHashMap<>();
            List<Map<String, Object>> rules = new ArrayList<>();

            // 规则示例
            Map<String, Object> rule1 = new LinkedHashMap<>();
            rule1.put("geosite", Arrays.asList("google", "github"));
            rule1.put("outbound", nodes.get(0).getName());
            rules.add(rule1);

            Map<String, Object> rule2 = new LinkedHashMap<>();
            rule2.put("geoip", "cn");
            rule2.put("outbound", "direct");
            rules.add(rule2);

            route.put("rules", rules);
            route.put("final", nodes.get(0).getName());
            config.put("route", route);

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);

        } catch (Exception e) {
            log.error("生成Sing-Box JSON配置失败", e);
            throw new BusinessException("配置生成失败: " + e.getMessage());
        }
    }
}
