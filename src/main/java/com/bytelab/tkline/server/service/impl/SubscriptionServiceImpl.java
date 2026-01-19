package com.bytelab.tkline.server.service.impl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytelab.tkline.server.config.RealityConfig;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订阅服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl extends ServiceImpl<SubscriptionMapper, Subscription> implements SubscriptionService {

    private final SubscriptionConverter subscriptionConverter;
    private final NodeSubscriptionRelationService nodeSubscriptionRelationService;
    private final NodeSubscriptionRelationMapper nodeSubscriptionRelationMapper;
    private final NodeMapper nodeMapper;
    private final RealityConfig realityConfig;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSubscription(SubscriptionCreateDTO createDTO) {
        // 1. 检查名称是否存在
        boolean exists = this.exists(new LambdaQueryWrapper<Subscription>()
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

        this.save(subscription);
        log.info("Subscription created: id={}, groupName={}, orderNo={}",
                subscription.getId(), subscription.getGroupName(), subscription.getOrderNo());

        return subscription.getId();
    }

    @Override
    public SubscriptionDTO getSubscriptionDetail(Long id) {
        Subscription subscription = this.getById(id);
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

        IPage<Subscription> result = this.page(page, wrapper);

        // 转换为DTO并计算节点数
        IPage<SubscriptionDTO> dtoPage = result.convert(subscription -> {
            SubscriptionDTO dto = subscriptionConverter.toDTO(subscription);

            // 计算总节点数
            Long totalNodeCount = nodeSubscriptionRelationMapper.selectCount(
                    new LambdaQueryWrapper<NodeSubscriptionRelation>()
                            .eq(NodeSubscriptionRelation::getSubscriptionId, subscription.getId())
                            .eq(NodeSubscriptionRelation::getDeleted, 0));
            dto.setNodeCount(totalNodeCount.intValue());

            // 计算可用节点数(状态为1=有效)
            Long availableNodeCount = nodeSubscriptionRelationMapper.selectCount(
                    new LambdaQueryWrapper<NodeSubscriptionRelation>()
                            .eq(NodeSubscriptionRelation::getSubscriptionId, subscription.getId())
                            .eq(NodeSubscriptionRelation::getStatus, 1)
                            .eq(NodeSubscriptionRelation::getDeleted, 0));
            dto.setAvailableNodeCount(availableNodeCount.intValue());

            return dto;
        });

        return dtoPage;
    }

    @Override
    public IPage<NodeDTO> pageSubscriptionNodes(
            Long subscriptionId, PageQueryDTO query) {
        // 创建分页对象
        Page<NodeDTO> page = new Page<>(query.getPage(), query.getPageSize());

        // 执行查询 - 直接返回包含绑定配置的 NodeDTO,支持名称和区域查询
        return baseMapper.selectNodesBySubscriptionId(page, subscriptionId, query.getName(), query.getRegion());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSubscription(SubscriptionUpdateDTO updateDTO) {
        // 1. 检查是否存在
        Subscription existing = this.getById(updateDTO.getId());
        if (existing == null) {
            throw new BusinessException("订阅不存在: " + updateDTO.getId());
        }

        // 2. 转换并更新
        subscriptionConverter.updateEntityFromDto(updateDTO, existing);
        existing.setUpdateBy("admin"); // TODO: current user

        boolean success = this.updateById(existing);
        if (!success) {
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
        Subscription subscription = this.getById(subscriptionId);
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
    public String generateYamlConfig(String orderNo, List<Long> nodeIds, String baseUrl) throws UnsupportedEncodingException {
        log.info("生成YAML配置: orderNo={}, nodeIds={}", orderNo, nodeIds);

        // 1. 查询订阅信息
        Subscription subscription = this.getOne(
                new LambdaQueryWrapper<Subscription>()
                        .eq(Subscription::getOrderNo, orderNo));

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
    public String generateJsonConfig(String orderNo, List<Long> nodeIdList, String baseUrl) throws UnsupportedEncodingException {
        log.info("生成JSON配置: orderNo={}, nodeIds={}", orderNo, nodeIdList);

        // 1. 查询订阅信息
        Subscription subscription = this.getOne(
                new LambdaQueryWrapper<Subscription>()
                        .eq(Subscription::getOrderNo, orderNo));

        if (subscription == null) {
            throw new BusinessException("订阅不存在: " + orderNo);
        }

        // 2. 查询订阅关联的节点
        List<Node> nodes = getSubscriptionNodesForConfig(subscription.getId(), nodeIdList);

        if (nodes.isEmpty()) {
            throw new BusinessException("订阅暂无可用节点");
        }

        // 3. 生成Sing-Box JSON配置
        return buildSingBoxJson(subscription, nodes, baseUrl);
    }

    @Override
    public String generateBase64Config(String orderNo, List<Long> nodeIdList, String baseUrl) {
        log.info("生成Base64配置: orderNo={}, nodeIds={}", orderNo, nodeIdList);

        // 1. 查询订阅信息
        Subscription subscription = this.getOne(
                new LambdaQueryWrapper<Subscription>()
                        .eq(Subscription::getOrderNo, orderNo));

        if (subscription == null) {
            throw new BusinessException("订阅不存在: " + orderNo);
        }

        // 2. 查询订阅关联的节点
        List<Node> nodes = getSubscriptionNodesForConfig(subscription.getId(), nodeIdList);

        if (nodes.isEmpty()) {
            throw new BusinessException("订阅暂无可用节点");
        }

        // 3. 生成URI列表并Base64编码
        StringBuilder uris = new StringBuilder();
        for (Node node : nodes) {
            uris.append(buildNodeUri(subscription, node)).append("\n");
        }

        return Base64.getEncoder().encodeToString(uris.toString().getBytes());
    }

    /**
     * 构建节点URI (hysteria2/vless/trojan)
     * 根据节点的 protocols 字段动态生成 URI
     */
    private String buildNodeUri(Subscription subscription, Node node) {
        String name = node.getName();
        String uuid = subscription.getOrderNo();
        StringBuilder uris = new StringBuilder();

        // 优先使用 protocols 字段
        if (StringUtils.isNotBlank(node.getProtocols())) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<String> protos = mapper.readValue(node.getProtocols(), new TypeReference<List<String>>() {});

                for (String proto : protos) {
                    int port = getProtocolPort(proto, node.getPort());
                    String uri = buildProtocolUri(proto, uuid, node, port, name);
                    if (uri != null) {
                        uris.append(uri).append("\n");
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse protocols for node '{}': {}", node.getName(), node.getProtocols(), e);
            }
        }

        return uris.toString().trim();
    }

    /**
     * 根据协议类型构建 URI
     */
    private String buildProtocolUri(String protocol, String uuid, Node node, int port, String name) {
        String protoLower = protocol.toLowerCase();
        String nodeName = name + "-" + protocol.toUpperCase();

        switch (protoLower) {
            case "hy2":
            case "hysteria2":
                // Hysteria2: hysteria2://password@host:port/?params
                StringBuilder hy2Uri = new StringBuilder();
                hy2Uri.append(String.format("hysteria2://%s@%s:%d/?sni=%s&obfs=salamander&obfs-password=NTdhMjdhMjAwMjRkYWEzYg==",
                        uuid, node.getIpAddress(), port, node.getIpAddress()));

                // 添加上行和下行带宽配置
                if (node.getUpstreamQuota() != null && node.getUpstreamQuota() > 0) {
                    hy2Uri.append("&up_mbps=").append(node.getUpstreamQuota());
                }
                if (node.getDownstreamQuota() != null && node.getDownstreamQuota() > 0) {
                    hy2Uri.append("&down_mbps=").append(node.getDownstreamQuota());
                }

                hy2Uri.append("#").append(nodeName);
                return hy2Uri.toString();

            case "vless":
            case "reality":
                // VLESS+Reality: vless://uuid@host:port?params
                String publicKey = realityConfig.getPublicKey();
                String shortId = realityConfig.getShortId();
                return String.format(
                        "vless://%s@%s:%d?encryption=none&security=reality&sni=www.cloudflare.com&fp=chrome&pbk=%s&sid=%s#%s",
                        uuid, node.getIpAddress(), port, publicKey, shortId, nodeName);

            case "trojan":
                // Trojan: trojan://password@host:port?params
                return String.format(
                        "trojan://%s@%s:%d?sni=%s&allowInsecure=0#%s",
                        uuid, node.getIpAddress(), port, node.getIpAddress(), nodeName);

            default:
                log.warn("Unknown protocol '{}' for node '{}'", protocol, node.getName());
                return null;
        }
    }

    /**
     * 获取订阅关联的节点列表(用于配置生成)
     *
     * @param subscriptionId 订阅ID
     * @param filterNodeIds  可选的节点ID过滤列表,为null时返回所有节点
     * @return 节点列表
     */
    private List<Node> getSubscriptionNodesForConfig(Long subscriptionId, List<Long> filterNodeIds) {
        // 查询订阅关联的节点ID
        List<NodeSubscriptionRelation> relations = nodeSubscriptionRelationMapper.selectList(
                new LambdaQueryWrapper<NodeSubscriptionRelation>()
                        .eq(NodeSubscriptionRelation::getSubscriptionId, subscriptionId)
                        .eq(NodeSubscriptionRelation::getStatus, 1) // 仅限有效绑定
                        .eq(NodeSubscriptionRelation::getDeleted, 0));

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
    private String buildClashYaml(Subscription subscription, List<Node> nodes, String baseUrl) throws UnsupportedEncodingException {
        // 订单号本身就是 UUID 格式，直接作为认证凭证
        String uuid = subscription.getOrderNo();

        // 按节点名排序
        List<Node> sortedNodes = nodes.stream()
                .sorted(Comparator.comparing(Node::getName))
                .collect(Collectors.toList());

        StringBuilder proxiesBuilder = new StringBuilder();
        List<String> proxyNames = new ArrayList<>();

        for (Node node : sortedNodes) {
            boolean generated = false;

            if (StringUtils.isNotBlank(node.getProtocols())) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    List<String> protos = mapper.readValue(node.getProtocols(), new TypeReference<>() {
                    });

                    if (!protos.isEmpty()) {
                        // 自定义协议排序：VLESS 第一，Hysteria2 第二，其他按字母排序
                        protos.sort((p1, p2) -> {
                            boolean isVless1 = "vless".equalsIgnoreCase(p1) || "reality".equalsIgnoreCase(p1);
                            boolean isVless2 = "vless".equalsIgnoreCase(p2) || "reality".equalsIgnoreCase(p2);
                            boolean isHy1 = "hy2".equalsIgnoreCase(p1) || "hysteria2".equalsIgnoreCase(p1);
                            boolean isHy2 = "hy2".equalsIgnoreCase(p2) || "hysteria2".equalsIgnoreCase(p2);

                            if (isVless1 && !isVless2) return -1;
                            if (!isVless1 && isVless2) return 1;
                            if (isHy1 && !isHy2) return -1;
                            if (!isHy1 && isHy2) return 1;
                            return p1.compareToIgnoreCase(p2);
                        });

                        for (String proto : protos) {
                            String proxyName = node.getName() + "-" + proto.toUpperCase();
                            // 根据协议获取对应的标准端口
                            int port = getProtocolPort(proto, node.getPort());

                            if ("hy2".equalsIgnoreCase(proto) || "hysteria2".equalsIgnoreCase(proto)) {
                                // Hysteria2 - 使用 UUID 作为 password
                                proxyNames.add("      - \"" + proxyName + "\"");
                                proxiesBuilder.append("  - name: \"").append(proxyName).append("\"\n");
                                proxiesBuilder.append("    type: hysteria2\n");
                                proxiesBuilder.append("    server: ").append(node.getIpAddress()).append("\n");
                                proxiesBuilder.append("    port: ").append(port).append("\n");
                                proxiesBuilder.append("    password: ").append(uuid).append("\n");
                                // 添加上行和下行带宽配置
                                if (node.getUpstreamQuota() != null && node.getUpstreamQuota() > 0) {
                                    proxiesBuilder.append("    up: ").append(node.getUpstreamQuota()).append("\n");
                                }
                                if (node.getDownstreamQuota() != null && node.getDownstreamQuota() > 0) {
                                    proxiesBuilder.append("    down: ").append(node.getDownstreamQuota()).append("\n");
                                }
                                proxiesBuilder.append("    sni: ").append(node.getIpAddress()).append("\n");
                                proxiesBuilder.append("    skip-cert-verify: false\n");
                                proxiesBuilder.append("    obfs: salamander\n");
                                proxiesBuilder.append("    obfs-password: NTdhMjdhMjAwMjRkYWEzYg==\n");
                                generated = true;
                            } else if ("vless".equalsIgnoreCase(proto) || "reality".equalsIgnoreCase(proto)) {
                                // VLESS+Reality
                                proxyNames.add("      - \"" + proxyName + "\"");
                                proxiesBuilder.append("  - name: \"").append(proxyName).append("\"\n");
                                proxiesBuilder.append("    type: vless\n");
                                proxiesBuilder.append("    server: ").append(node.getIpAddress()).append("\n");
                                proxiesBuilder.append("    port: ").append(port).append("\n");
                                proxiesBuilder.append("    uuid: ").append(uuid).append("\n");
                                proxiesBuilder.append("    network: tcp\n");
                                proxiesBuilder.append("    tls: true\n");
                                proxiesBuilder.append("    udp: true\n");
                                // proxiesBuilder.append("    flow: xtls-rprx-vision\n");
                                proxiesBuilder.append("    servername: www.cloudflare.com\n");
                                proxiesBuilder.append("    reality-opts:\n");
                                // 使用配置中的 Reality 公钥
                                proxiesBuilder.append("      public-key: ").append(realityConfig.getPublicKey()).append("\n");
                                proxiesBuilder.append("      short-id: ").append(realityConfig.getShortId()).append("\n");
                                proxiesBuilder.append("    client-fingerprint: chrome\n");
                                generated = true;
                            } else if ("trojan".equalsIgnoreCase(proto)) {
                                // Trojan - 使用 UUID 作为 password
                                proxyNames.add("      - \"" + proxyName + "\"");
                                proxiesBuilder.append("  - name: \"").append(proxyName).append("\"\n");
                                proxiesBuilder.append("    type: trojan\n");
                                proxiesBuilder.append("    server: ").append(node.getIpAddress()).append("\n");
                                proxiesBuilder.append("    port: ").append(port).append("\n");
                                proxiesBuilder.append("    password: ").append(uuid).append("\n");
                                proxiesBuilder.append("    sni: ").append(node.getIpAddress()).append("\n");
                                proxiesBuilder.append("    skip-cert-verify: false\n");
                                proxiesBuilder.append("    udp: true\n");
                                generated = true;
                            } else {
                                log.warn("Unknown protocol '{}' for node '{}'", proto, node.getName());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse protocols for node '{}': {}", node.getName(), node.getProtocols(), e);
                }
            }

            if (!generated) {
                // Fallback to legacy port-based logic
                String proxyName = node.getName();
                proxyNames.add("      - \"" + proxyName + "\"");
                if (node.getPort() == 8443) {
                    // VLESS+Reality (legacy) - 端口 8443
                    proxiesBuilder.append("  - name: \"").append(proxyName).append("\"\n");
                    proxiesBuilder.append("    type: vless\n");
                    proxiesBuilder.append("    server: ").append(node.getIpAddress()).append("\n");
                    proxiesBuilder.append("    port: ").append(node.getPort()).append("\n");
                    proxiesBuilder.append("    uuid: ").append(uuid).append("\n");
                    proxiesBuilder.append("    network: tcp\n");
                    proxiesBuilder.append("    tls: true\n");
                    proxiesBuilder.append("    udp: true\n");
                    // proxiesBuilder.append("    flow: xtls-rprx-vision\n");
                    proxiesBuilder.append("    servername: www.cloudflare.com\n");
                    proxiesBuilder.append("    reality-opts:\n");
                    // 使用配置中的 Reality 公钥
                    proxiesBuilder.append("      public-key: ").append(realityConfig.getPublicKey()).append("\n");
                    proxiesBuilder.append("      short-id: ").append(realityConfig.getShortId()).append("\n");
                    proxiesBuilder.append("    client-fingerprint: chrome\n");
                } else {
                    // Hysteria2 (legacy) - 使用 UUID 作为 password
                    proxiesBuilder.append("  - name: \"").append(proxyName).append("\"\n");
                    proxiesBuilder.append("    type: hysteria2\n");
                    proxiesBuilder.append("    server: ").append(node.getIpAddress()).append("\n");
                    proxiesBuilder.append("    port: ").append(node.getPort()).append("\n");
                    proxiesBuilder.append("    password: ").append(uuid).append("\n");
                    // 添加上行和下行带宽配置
                    if (node.getUpstreamQuota() != null && node.getUpstreamQuota() > 0) {
                        proxiesBuilder.append("    up: ").append(node.getUpstreamQuota()).append("\n");
                    }
                    if (node.getDownstreamQuota() != null && node.getDownstreamQuota() > 0) {
                        proxiesBuilder.append("    down: ").append(node.getDownstreamQuota()).append("\n");
                    }
                    proxiesBuilder.append("    sni: ").append(node.getIpAddress()).append("\n");
                    proxiesBuilder.append("    skip-cert-verify: false\n");
                    proxiesBuilder.append("    obfs: salamander\n");
                    proxiesBuilder.append("    obfs-password: NTdhMjdhMjAwMjRkYWEzYg==\n");
                }
            }
        }

        // 去除末尾多余的换行，但保留缩进
        String proxiesStr = proxiesBuilder.toString();
        if (proxiesStr.endsWith("\n")) {
            proxiesStr = proxiesStr.substring(0, proxiesStr.length() - 1);
        }
        String proxyNamesStr = String.join("\n", proxyNames);

        String config = """
# Clash Meta 配置文件模板
# 订阅组: """ + subscription.getGroupName() + "\n" + """
# 订阅编号: """ + subscription.getOrderNo() + "\n" + """

proxies:
""" + (proxiesStr.isEmpty() ? "" : proxiesStr + "\n") + """

proxy-groups:
  - name: PROXY
    type: select
    proxies:
""" + (proxyNamesStr.isEmpty() ? "" : proxyNamesStr + "\n") + """
      - DIRECT

rules:
  # 局域网直连
  - IP-CIDR,192.168.0.0/16,DIRECT,no-resolve
  - IP-CIDR,10.0.0.0/8,DIRECT,no-resolve
  - IP-CIDR,172.16.0.0/12,DIRECT,no-resolve
  - IP-CIDR,127.0.0.0/8,DIRECT,no-resolve
  - IP-CIDR,169.254.0.0/16,DIRECT,no-resolve
  - IP-CIDR6,fe80::/10,DIRECT,no-resolve
  # 中国大陆流量直连
  - GEOIP,CN,DIRECT
  - GEOSITE,CN,DIRECT
  # 其他流量走代理
  - MATCH,PROXY
""";

        return config;
    }

    /**
     * 构建Sing-Box JSON配置
     */
    private String buildSingBoxJson(Subscription subscription, List<Node> nodes, String baseUrl) throws UnsupportedEncodingException {
        // 订单号本身就是 UUID 格式，直接作为认证凭证
        String uuid = subscription.getOrderNo();

        // 按节点名排序
        List<Node> sortedNodes = nodes.stream()
                .sorted(Comparator.comparing(Node::getName))
                .collect(Collectors.toList());

        StringBuilder outboundsBuilder = new StringBuilder();
        String firstTag = "direct";

        for (Node node : sortedNodes) {
            boolean generated = false;

            if (StringUtils.isNotBlank(node.getProtocols())) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    List<String> protos = mapper.readValue(node.getProtocols(), new TypeReference<List<String>>() {});

                    if (!protos.isEmpty()) {
                        // 自定义协议排序：VLESS 第一，Hysteria2 第二，其他按字母排序
                        protos.sort((p1, p2) -> {
                            boolean isVless1 = "vless".equalsIgnoreCase(p1) || "reality".equalsIgnoreCase(p1);
                            boolean isVless2 = "vless".equalsIgnoreCase(p2) || "reality".equalsIgnoreCase(p2);
                            boolean isHy1 = "hy2".equalsIgnoreCase(p1) || "hysteria2".equalsIgnoreCase(p1);
                            boolean isHy2 = "hy2".equalsIgnoreCase(p2) || "hysteria2".equalsIgnoreCase(p2);

                            if (isVless1 && !isVless2) return -1;
                            if (!isVless1 && isVless2) return 1;
                            if (isHy1 && !isHy2) return -1;
                            if (!isHy1 && isHy2) return 1;
                            return p1.compareToIgnoreCase(p2);
                        });

                        for (String proto : protos) {
                            String tag = node.getName() + "-" + proto.toUpperCase();
                            // 根据协议获取对应的标准端口
                            int port = getProtocolPort(proto, node.getPort());

                            if ("hy2".equalsIgnoreCase(proto) || "hysteria2".equalsIgnoreCase(proto)) {
                                // Hysteria2 - 使用 UUID 作为 password
                                outboundsBuilder.append("        {\n");
                                outboundsBuilder.append("            \"tag\": \"").append(tag).append("\",\n");
                                outboundsBuilder.append("            \"type\": \"hysteria2\",\n");
                                outboundsBuilder.append("            \"server\": \"").append(node.getIpAddress()).append("\",\n");
                                outboundsBuilder.append("            \"server_port\": ").append(port).append(",\n");
                                outboundsBuilder.append("            \"password\": \"").append(uuid).append("\",\n");
                                // 添加上行和下行带宽配置
                                if (node.getUpstreamQuota() != null && node.getUpstreamQuota() > 0) {
                                    outboundsBuilder.append("            \"up_mbps\": ").append(node.getUpstreamQuota()).append(",\n");
                                }
                                if (node.getDownstreamQuota() != null && node.getDownstreamQuota() > 0) {
                                    outboundsBuilder.append("            \"down_mbps\": ").append(node.getDownstreamQuota()).append(",\n");
                                }
                                outboundsBuilder.append("            \"obfs\": {\n");
                                outboundsBuilder.append("                \"type\": \"salamander\",\n");
                                outboundsBuilder.append("                \"password\": \"NTdhMjdhMjAwMjRkYWEzYg==\"\n");
                                outboundsBuilder.append("            },\n");
                                outboundsBuilder.append("            \"tls\": {\n");
                                outboundsBuilder.append("                \"enabled\": true,\n");
                                outboundsBuilder.append("                \"server_name\": \"").append(node.getIpAddress()).append("\",\n");
                                outboundsBuilder.append("                \"alpn\": [\n");
                                outboundsBuilder.append("                    \"h3\"\n");
                                outboundsBuilder.append("                ]\n");
                                outboundsBuilder.append("            }\n");
                                outboundsBuilder.append("        },\n");
                                if ("direct".equals(firstTag)) firstTag = tag;
                                generated = true;
                            } else if ("vless".equalsIgnoreCase(proto) || "reality".equalsIgnoreCase(proto)) {
                                // VLESS+Reality
                                outboundsBuilder.append("        {\n");
                                outboundsBuilder.append("            \"tag\": \"").append(tag).append("\",\n");
                                outboundsBuilder.append("            \"type\": \"vless\",\n");
                                outboundsBuilder.append("            \"server\": \"").append(node.getIpAddress()).append("\",\n");
                                outboundsBuilder.append("            \"server_port\": ").append(port).append(",\n");
                                outboundsBuilder.append("            \"uuid\": \"").append(uuid).append("\",\n"); // 客户端身份认证凭证
                                // outboundsBuilder.append("            \"flow\": \"xtls-rprx-vision\",\n");
                                outboundsBuilder.append("            \"tls\": {\n");
                                outboundsBuilder.append("                \"enabled\": true,\n");
                                outboundsBuilder.append("                \"server_name\": \"www.cloudflare.com\",\n"); // 服务器域名 告诉服务器要伪装成哪个网站
                                outboundsBuilder.append("                \"utls\": {\n"); // 客户端指纹 告诉服务器要使用哪个指纹 模拟真实浏览器的 TLS 握手指纹
                                outboundsBuilder.append("                    \"enabled\": true,\n");
                                outboundsBuilder.append("                    \"fingerprint\": \"chrome\"\n"); // 模拟 Chrome 浏览器
                                outboundsBuilder.append("                },\n");
                                outboundsBuilder.append("                \"reality\": {\n");
                                outboundsBuilder.append("                    \"enabled\": true,\n");
                                // 使用配置中的 Reality 公钥
                                outboundsBuilder.append("                    \"public_key\": \"").append(realityConfig.getPublicKey()).append("\",\n");
                                outboundsBuilder.append("                    \"short_id\": \"").append(realityConfig.getShortId()).append("\"\n");
                                outboundsBuilder.append("                }\n");
                                outboundsBuilder.append("            }\n");
                                outboundsBuilder.append("        },\n");
                                if ("direct".equals(firstTag)) firstTag = tag;
                                generated = true;
                            } else if ("trojan".equalsIgnoreCase(proto)) {
                                // Trojan - 使用 UUID 作为 password
                                outboundsBuilder.append("        {\n");
                                outboundsBuilder.append("            \"tag\": \"").append(tag).append("\",\n");
                                outboundsBuilder.append("            \"type\": \"trojan\",\n");
                                outboundsBuilder.append("            \"server\": \"").append(node.getIpAddress()).append("\",\n");
                                outboundsBuilder.append("            \"server_port\": ").append(port).append(",\n");
                                outboundsBuilder.append("            \"password\": \"").append(uuid).append("\",\n");
                                outboundsBuilder.append("            \"tls\": {\n");
                                outboundsBuilder.append("                \"enabled\": true,\n");
                                outboundsBuilder.append("                \"server_name\": \"").append(node.getIpAddress()).append("\",\n");
                                outboundsBuilder.append("                \"alpn\": [\n");
                                outboundsBuilder.append("                    \"h2\",\n");
                                outboundsBuilder.append("                    \"http/1.1\"\n");
                                outboundsBuilder.append("                ]\n");
                                outboundsBuilder.append("            }\n");
                                outboundsBuilder.append("        },\n");
                                if ("direct".equals(firstTag)) firstTag = tag;
                                generated = true;
                            } else {
                                log.warn("Unknown protocol '{}' for node '{}'", proto, node.getName());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse protocols for node '{}': {}", node.getName(), node.getProtocols(), e);
                }
            }

            if (!generated) {
                // Fallback to legacy port-based logic
                String tag = node.getName();
                if (node.getPort() == 8443) {
                    // VLESS+Reality (legacy) - 端口 8443
                    outboundsBuilder.append("        {\n");
                    outboundsBuilder.append("            \"tag\": \"").append(tag).append("\",\n");
                    outboundsBuilder.append("            \"type\": \"vless\",\n");
                    outboundsBuilder.append("            \"server\": \"").append(node.getIpAddress()).append("\",\n");
                    outboundsBuilder.append("            \"server_port\": ").append(node.getPort()).append(",\n");
                    outboundsBuilder.append("            \"uuid\": \"").append(uuid).append("\",\n");
                    // outboundsBuilder.append("            \"flow\": \"xtls-rprx-vision\",\n");
                    outboundsBuilder.append("            \"tls\": {\n");
                    outboundsBuilder.append("                \"enabled\": true,\n");
                    outboundsBuilder.append("                \"server_name\": \"www.cloudflare.com\",\n");
                    outboundsBuilder.append("                \"utls\": {\n");
                    outboundsBuilder.append("                    \"enabled\": true,\n");
                    outboundsBuilder.append("                    \"fingerprint\": \"chrome\"\n");
                    outboundsBuilder.append("                },\n");
                    outboundsBuilder.append("                \"reality\": {\n");
                    outboundsBuilder.append("                    \"enabled\": true,\n");
                    outboundsBuilder.append("                    \"public_key\": \"").append(realityConfig.getPublicKey()).append("\",\n");
                    outboundsBuilder.append("                    \"short_id\": \"").append(realityConfig.getShortId()).append("\"\n");
                    outboundsBuilder.append("                }\n");
                    outboundsBuilder.append("            }\n");
                    outboundsBuilder.append("        },\n");
                } else {
                    // Hysteria2 (legacy) - 使用 UUID 作为 password
                    outboundsBuilder.append("        {\n");
                    outboundsBuilder.append("            \"tag\": \"").append(tag).append("\",\n");
                    outboundsBuilder.append("            \"type\": \"hysteria2\",\n");
                    outboundsBuilder.append("            \"server\": \"").append(node.getIpAddress()).append("\",\n");
                    outboundsBuilder.append("            \"server_port\": ").append(node.getPort()).append(",\n");
                    outboundsBuilder.append("            \"password\": \"").append(uuid).append("\",\n");
                    // 添加上行和下行带宽配置
                    if (node.getUpstreamQuota() != null && node.getUpstreamQuota() > 0) {
                        outboundsBuilder.append("            \"up_mbps\": ").append(node.getUpstreamQuota()).append(",\n");
                    }
                    if (node.getDownstreamQuota() != null && node.getDownstreamQuota() > 0) {
                        outboundsBuilder.append("            \"down_mbps\": ").append(node.getDownstreamQuota()).append(",\n");
                    }
                    outboundsBuilder.append("            \"obfs\": {\n");
                    outboundsBuilder.append("                \"type\": \"salamander\",\n");
                    outboundsBuilder.append("                \"password\": \"NTdhMjdhMjAwMjRkYWEzYg==\"\n");
                    outboundsBuilder.append("            },\n");
                    outboundsBuilder.append("            \"tls\": {\n");
                    outboundsBuilder.append("                \"enabled\": true,\n");
                    outboundsBuilder.append("                \"server_name\": \"").append(node.getIpAddress()).append("\",\n");
                    outboundsBuilder.append("                \"alpn\": [\n");
                    outboundsBuilder.append("                    \"h3\"\n");
                    outboundsBuilder.append("                ]\n");
                    outboundsBuilder.append("            }\n");
                    outboundsBuilder.append("        },\n");
                }
                if ("direct".equals(firstTag)) firstTag = tag;
            }
        }

        String finalOutbound = firstTag;

        // 移除 outboundsBuilder 末尾的逗号和换行
        String outboundsStr = outboundsBuilder.toString().trim();
        if (outboundsStr.endsWith(",")) {
            outboundsStr = outboundsStr.substring(0, outboundsStr.length() - 1);
        }

        String config = """
{
    "outbounds": [
""" + (outboundsStr.isEmpty() ? "" : outboundsStr + ",\n") + """
        {
            "type": "direct",
            "tag": "direct"
        },
        {
            "type": "dns",
            "tag": "dns-out"
        },
        {
            "type": "block",
            "tag": "block"
        }
    ],
    "dns": {
        "servers": [
            {
                "tag": "google",
                "address": "https://8.8.8.8/dns-query",
                "address_resolver": "local"
            },
            {
                "tag": "cloudflare",
                "address": "https://1.1.1.1/dns-query",
                "address_resolver": "local"
            },
            {
                "tag": "local",
                "address": "223.5.5.5",
                "detour": "direct"
            },
            {
                "tag": "block",
                "address": "rcode://success"
            }
        ],
        "rules": [
            {
                "outbound": "any",
                "server": "local"
            },
            {
                "rule_set": "geosite-category-ads-all",
                "server": "block"
            },
            {
                "rule_set": "geosite-cn",
                "server": "local"
            },
            {
                "clash_mode": "direct",
                "server": "local"
            },
            {
                "clash_mode": "global",
                "server": "google"
            }
        ],
        "final": "google",
        "strategy": "prefer_ipv4"
    },
    "route": {
        "auto_detect_interface": true,
        "final": \"""" + finalOutbound + """
",
        "rule_set": [
            {
                "tag": "geosite-cn",
                "type": "remote",
                "format": "binary",
                "url": "https://raw.githubusercontent.com/SagerNet/sing-geosite/rule-set/geosite-cn.srs",
                "download_detour": "direct"
            },
            {
                "tag": "geoip-cn",
                "type": "remote",
                "format": "binary",
                "url": "https://raw.githubusercontent.com/SagerNet/sing-geoip/rule-set/geoip-cn.srs",
                "download_detour": "direct"
            },
            {
                "tag": "geosite-category-ads-all",
                "type": "remote",
                "format": "binary",
                "url": "https://raw.githubusercontent.com/SagerNet/sing-geosite/rule-set/geosite-category-ads-all.srs",
                "download_detour": "direct"
            }
        ],
        "rules": [
            {
                "protocol": "dns",
                "outbound": "dns-out"
            },
            {
                "rule_set": "geosite-category-ads-all",
                "outbound": "block"
            },
            {
                "ip_is_private": true,
                "outbound": "direct"
            },
            {
                "clash_mode": "direct",
                "outbound": "direct"
            },
            {
                "clash_mode": "global",
                "outbound": \"""" + finalOutbound + """
"
            },
            {
                "rule_set": [
                    "geoip-cn",
                    "geosite-cn"
                ],
                "outbound": "direct"
            }
        ]
    }
}
""";

        return config;
    }

    /**
     * 从模板中渲染定义的块（类似 Helm define）
     */
    private String extractBlock(String template, String blockName) {
        String startTag = "{{ define \"" + blockName + "\" }}";
        String endTag = "{{ end }}";

        int start = template.indexOf(startTag);
        int end = template.indexOf(endTag, start);

        if (start != -1 && end != -1) {
            return template.substring(start + startTag.length(), end);
        }
        return "";
    }

    /**
     * 清理模板定义标记及空行
     */
    private String cleanTemplate(String config) {
        // 移除 {{ define ... }} 和 {{ end }} 以及前导/后缀标记
        return config.replaceAll("(?m)^.*\\{\\{ (define|end) .*\\}\\}\r?\n?", "")
                .replaceAll("(?m)^[ \t]*\r?\n", "") // 移除纯空白行
                .trim();
    }

    /**
     * 根据协议类型获取标准端口
     *
     * @param protocol 协议类型 (vless, hy2, trojan, tuic等)
     * @param fallbackPort 如果协议未定义标准端口，使用的回退端口
     * @return 协议对应的端口号
     */
    private int getProtocolPort(String protocol, int fallbackPort) {
        if (protocol == null) {
            return fallbackPort;
        }

        // 标准协议端口映射（与服务端配置一致）
        return switch (protocol.toLowerCase()) {
            case "vless", "reality" -> 8443;      // VLESS+Reality 使用 8443
            case "hy2", "hysteria2" -> 7443;       // Hysteria2 使用 7443
            case "trojan" -> 9443;                // Trojan 使用 9443
            case "tuic" -> 10443;                 // TUIC 使用 10443
            default -> {
                log.warn("Unknown protocol '{}', using fallback port: {}", protocol, fallbackPort);
                yield fallbackPort;
            }
        };
    }

    /**
     * 加载模板文件
     */
    private String loadTemplate(String templateName) {
        try {
            try (java.io.InputStream is = getClass().getClassLoader()
                    .getResourceAsStream("templates/subscription/" + templateName)) {
                if (is == null) {
                    throw new BusinessException("配置模板不存在: " + templateName);
                }
                try (java.util.Scanner scanner = new java.util.Scanner(is, "UTF-8")) {
                    return scanner.useDelimiter("\\A").next();
                }
            }
        } catch (Exception e) {
            log.error("加载模板失败: {}", templateName, e);
            throw new BusinessException("加载配置模板失败: " + e.getMessage());
        }
    }
}
