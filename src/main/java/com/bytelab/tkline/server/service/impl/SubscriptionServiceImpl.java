package com.bytelab.tkline.server.service.impl;

import ch.qos.logback.core.util.StringUtil;
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
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionConverter subscriptionConverter;
    private final NodeSubscriptionRelationService nodeSubscriptionRelationService;
    private final NodeSubscriptionRelationMapper nodeSubscriptionRelationMapper;
    private final NodeMapper nodeMapper;

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
    public String generateYamlConfig(String orderNo, List<Long> nodeIds, String baseUrl) throws UnsupportedEncodingException {
        log.info("生成YAML配置: orderNo={}, nodeIds={}", orderNo, nodeIds);

        // 1. 查询订阅信息
        Subscription subscription = subscriptionMapper.selectOne(
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
        Subscription subscription = subscriptionMapper.selectOne(
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
        Subscription subscription = subscriptionMapper.selectOne(
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
     * 构建节点URI (hysteria2/vless)
     */
    private String buildNodeUri(Subscription subscription, Node node) {
        String name = node.getName();
        String password = subscription.getOrderNo(); // 使用订单号作为密码/种子

        // 简单模拟节点类型判断，实际业务中 node 表可能需要 protocol 字段
        // 这里基于 port 或名称简单区分，或者根据需求同时返回两种协议
        if (node.getPort() == 443) {
            // VLESS+Reality
            String uuid = UUID.nameUUIDFromBytes(password.getBytes()).toString();
            return String.format(
                    "vless://%s@%s:%d?encryption=none&flow=xtls-rprx-vision&security=reality&sni=www.cloudflare.com&fp=chrome&pbk=YOUR_REALITY_PUBLIC_KEY&sid=a1b2c3d4#%s",
                    uuid, node.getIpAddress(), node.getPort(), name);
        } else {
            // Hysteria2
            return String.format(
                    "hysteria2://%s@%s:%d/?sni=%s&obfs=salamander&obfs-password=NTdhMjdhMjAwMjRkYWEzYg==#%s",
                    password, node.getIpAddress(), node.getPort(), node.getIpAddress(), name);
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
        String password = subscription.getOrderNo();

        // 按节点名排序
        List<Node> sortedNodes = nodes.stream()
                .sorted(Comparator.comparing(Node::getName))
                .collect(Collectors.toList());

        StringBuilder proxiesBuilder = new StringBuilder();
        List<String> proxyNames = new ArrayList<>();

        for (Node node : sortedNodes) {
            String uuid = UUID.nameUUIDFromBytes(StringUtils.getBytes(password,"UTF-8")).toString();

            if (node.getPort() == 443) {
                // VLESS+Reality
                proxiesBuilder.append("  - name: \"").append(node.getName()).append("\"\n");
                proxiesBuilder.append("    type: vless\n");
                proxiesBuilder.append("    server: ").append(node.getIpAddress()).append("\n");
                proxiesBuilder.append("    port: ").append(node.getPort()).append("\n");
                proxiesBuilder.append("    uuid: ").append(uuid).append("\n");
                proxiesBuilder.append("    network: tcp\n");
                proxiesBuilder.append("    tls: true\n");
                proxiesBuilder.append("    udp: true\n");
                proxiesBuilder.append("    flow: xtls-rprx-vision\n");
                proxiesBuilder.append("    servername: www.cloudflare.com\n");
                proxiesBuilder.append("    reality-opts:\n");
                proxiesBuilder.append("      public-key: YOUR_REALITY_PUBLIC_KEY\n");
                proxiesBuilder.append("      short-id: a1b2c3d4\n");
                proxiesBuilder.append("    client-fingerprint: chrome\n");
            } else {
                // Hysteria2
                proxiesBuilder.append("  - name: \"").append(node.getName()).append("\"\n");
                proxiesBuilder.append("    type: hysteria2\n");
                proxiesBuilder.append("    server: ").append(node.getIpAddress()).append("\n");
                proxiesBuilder.append("    port: ").append(node.getPort()).append("\n");
                proxiesBuilder.append("    password: ").append(password).append("\n");
                proxiesBuilder.append("    sni: ").append(node.getIpAddress()).append("\n");
                proxiesBuilder.append("    skip-cert-verify: false\n");
                proxiesBuilder.append("    obfs: salamander\n");
                proxiesBuilder.append("    obfs-password: NTdhMjdhMjAwMjRkYWEzYg==\n");
            }
            proxyNames.add("      - \"" + node.getName() + "\"");
        }

        String proxiesStr = proxiesBuilder.toString().trim();
        String proxyNamesStr = String.join("\n", proxyNames);

        String config = """
# Clash Meta 配置文件模板
# 订阅组: """ + subscription.getGroupName() + """
# 订阅编号: """ + subscription.getOrderNo() + """

port: 7890
socks-port: 7891
allow-lan: false
mode: rule
log-level: info
external-controller: 127.0.0.1:9090

proxies:
""" + (proxiesStr.isEmpty() ? "" : proxiesStr + "\n") + """

proxy-groups:
  - name: PROXY
    type: select
    proxies:
""" + (proxyNamesStr.isEmpty() ? "" : proxyNamesStr + "\n") + """
      - DIRECT

rules:
  - GEOIP,CN,DIRECT
  - MATCH,PROXY
""";

        return config;
    }

    /**
     * 构建Sing-Box JSON配置
     */
    private String buildSingBoxJson(Subscription subscription, List<Node> nodes, String baseUrl) throws UnsupportedEncodingException {
        String password = subscription.getOrderNo();

        // 按节点名排序
        List<Node> sortedNodes = nodes.stream()
                .sorted(Comparator.comparing(Node::getName))
                .collect(Collectors.toList());

        StringBuilder outboundsBuilder = new StringBuilder();

        for (Node node : sortedNodes) {
            String uuid = UUID.nameUUIDFromBytes(StringUtils.getBytes(password,"UTF-8")).toString();

            if (node.getPort() == 443) {
                // VLESS+Reality
                outboundsBuilder.append("        {\n");
                outboundsBuilder.append("            \"tag\": \"").append(node.getName()).append("\",\n");
                outboundsBuilder.append("            \"type\": \"vless\",\n");
                outboundsBuilder.append("            \"server\": \"").append(node.getIpAddress()).append("\",\n");
                outboundsBuilder.append("            \"server_port\": ").append(node.getPort()).append(",\n");
                outboundsBuilder.append("            \"uuid\": \"").append(uuid).append("\",\n");
                outboundsBuilder.append("            \"flow\": \"xtls-rprx-vision\",\n");
                outboundsBuilder.append("            \"tls\": {\n");
                outboundsBuilder.append("                \"enabled\": true,\n");
                outboundsBuilder.append("                \"server_name\": \"www.cloudflare.com\",\n");
                outboundsBuilder.append("                \"utls\": {\n");
                outboundsBuilder.append("                    \"enabled\": true,\n");
                outboundsBuilder.append("                    \"fingerprint\": \"chrome\"\n");
                outboundsBuilder.append("                },\n");
                outboundsBuilder.append("                \"reality\": {\n");
                outboundsBuilder.append("                    \"enabled\": true,\n");
                outboundsBuilder.append("                    \"public_key\": \"YOUR_REALITY_PUBLIC_KEY\",\n");
                outboundsBuilder.append("                    \"short_id\": \"a1b2c3d4\"\n");
                outboundsBuilder.append("                }\n");
                outboundsBuilder.append("            }\n");
            } else {
                // Hysteria2
                outboundsBuilder.append("        {\n");
                outboundsBuilder.append("            \"tag\": \"").append(node.getName()).append("\",\n");
                outboundsBuilder.append("            \"type\": \"hysteria2\",\n");
                outboundsBuilder.append("            \"server\": \"").append(node.getIpAddress()).append("\",\n");
                outboundsBuilder.append("            \"server_port\": ").append(node.getPort()).append(",\n");
                outboundsBuilder.append("            \"password\": \"").append(password).append("\",\n");
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
            }
            outboundsBuilder.append("        },\n");
        }

        String finalOutbound = sortedNodes.isEmpty() ? "direct" : sortedNodes.get(0).getName();

        String config = """
{
    "log": {
        "level": "info",
        "timestamp": true
    },
    "inbounds": [
        {
            "type": "mixed",
            "tag": "mixed-in",
            "listen": "127.0.0.1",
            "listen_port": 7890
        }
    ],
    "outbounds": [
""" + outboundsBuilder.toString().trim() + """
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
    "route": {
        "auto_detect_interface": true,
        "final": \"""" + finalOutbound + """
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
