package com.bytelab.tkline.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytelab.tkline.server.config.RealityConfig;
import com.bytelab.tkline.server.converter.NodeConverter;
import com.bytelab.tkline.server.converter.SubscriptionConverter;
import com.bytelab.tkline.server.converter.SubscriptionWithBindingConverter;
import com.bytelab.tkline.server.dto.PageQueryDTO;
import com.bytelab.tkline.server.dto.node.NodeCreateDTO;
import com.bytelab.tkline.server.dto.node.NodeDTO;
import com.bytelab.tkline.server.dto.node.NodeHeartbeatDTO;
import com.bytelab.tkline.server.dto.node.NodeQueryDTO;
import com.bytelab.tkline.server.dto.node.NodeUpdateDTO;
import com.bytelab.tkline.server.dto.subscription.SubscriptionDTO;
import com.bytelab.tkline.server.entity.Node;
import com.bytelab.tkline.server.entity.Subscription;
import com.bytelab.tkline.server.exception.BusinessException;
import com.bytelab.tkline.server.mapper.NodeMapper;
import com.bytelab.tkline.server.mapper.NodeSubscriptionRelationMapper;
import com.bytelab.tkline.server.service.NodeService;
import com.bytelab.tkline.server.util.HttpUtil;
import com.bytelab.tkline.server.vo.SubscriptionWithBindingVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 节点服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NodeServiceImpl extends ServiceImpl<NodeMapper, Node> implements NodeService {

    private final NodeConverter nodeConverter;

    private final SubscriptionConverter subscriptionConverter;

    private final SubscriptionWithBindingConverter subscriptionWithBindingConverter;

    private final NodeSubscriptionRelationMapper nodeSubscriptionRelationMapper;

    private final ObjectMapper objectMapper;

    private final RealityConfig realityConfig;

    @Value("${api.service.secret}")
    private String apiServiceSecret;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createNode(NodeCreateDTO createDTO) {
        // 1. 检查名称是否存在
        System.out.println("Checking node name: " + createDTO.getName());
        boolean exists = this.exists(new LambdaQueryWrapper<Node>()
                .eq(Node::getName, createDTO.getName()));
        if (exists) {
            throw new BusinessException("节点名称已存在: " + createDTO.getName());
        }

        // 2. 转换并保存
        Node node = nodeConverter.toEntity(createDTO);

        this.save(node);
        log.info("Node created: id={}, name={}", node.getId(), node.getName());

        return node.getId();
    }

    @Override
    public NodeDTO getNodeDetail(Long id) {
        Node node = this.getById(id);
        if (node == null) {
            throw new BusinessException("节点不存在: " + id);
        }
        return nodeConverter.toDTO(node);
    }

    @Override
    public IPage<NodeDTO> pageNodes(
            NodeQueryDTO query) {
        Page<Node> page = new Page<>(
                query.getPage(), query.getPageSize());

        LambdaQueryWrapper<Node> wrapper = new LambdaQueryWrapper<>();
        if (query.getName() != null && !query.getName().isEmpty()) {
            wrapper.like(Node::getName, query.getName());
        }
        if (query.getDomain() != null && !query.getDomain().isEmpty()) {
            wrapper.like(Node::getDomain, query.getDomain());
        }
        if (query.getIpAddress() != null && !query.getIpAddress().isEmpty()) {
            wrapper.eq(Node::getIpAddress, query.getIpAddress());
        }
        if (query.getRegion() != null && !query.getRegion().isEmpty()) {
            wrapper.like(Node::getRegion, query.getRegion());
        }
        // ignore status for now if type mismatch or handle if needed

        wrapper.orderByDesc(Node::getId);

        IPage<Node> result = this.page(page, wrapper);

        // 转换为 DTO 并填充订阅数
        return result.convert(node -> {
            NodeDTO dto = nodeConverter.toDTO(node);
            // 统计该节点的订阅数
            Integer subscriptionCount = baseMapper.countSubscriptionsByNodeId(node.getId());
            dto.setSubscriptionCount(subscriptionCount != null ? subscriptionCount : 0);
            return dto;
        });
    }

    @Override
    public void heartbeat(NodeHeartbeatDTO heartbeatDTO) {
        Node node = this.getById(heartbeatDTO.getId());
        if (node == null) {
            throw new RuntimeException("节点不存在");
        }
        node.setLastHeartbeatTime(LocalDateTime.now());
        this.updateById(node);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNode(NodeUpdateDTO updateDTO) {
        Node existingNode = this.getById(updateDTO.getId());
        if (existingNode == null) {
            throw new BusinessException("节点不存在: " + updateDTO.getId());
        }

        // 创建 UpdateWrapper 用于更新
        LambdaUpdateWrapper<Node> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Node::getId, updateDTO.getId());

        // 更新基本字段
        updateWrapper.set(Node::getName, updateDTO.getName())
                .set(Node::getDomain, updateDTO.getDomain())
                .set(Node::getIpAddress, updateDTO.getIpAddress())
                .set(Node::getPort, updateDTO.getPort())
                .set(Node::getRegion, updateDTO.getRegion())
                .set(Node::getDescription, updateDTO.getDescription())
                .set(Node::getProtocols, updateDTO.getProtocols())
                .set(Node::getUpstreamQuota, updateDTO.getUpstreamQuota())
                .set(Node::getDownstreamQuota, updateDTO.getDownstreamQuota());

        // 更新状态（如果提供了该字段）：online 布尔值转换为 status 整数 (0=离线, 1=在线)
        if (updateDTO.getOnline() != null) {
            updateWrapper.set(Node::getStatus, updateDTO.getOnline() ? 1 : 0);
        }

        this.update(null, updateWrapper);
        log.info("Node updated: id={}", updateDTO.getId());
    }

    @Override
    public IPage<SubscriptionDTO> pageNodeSubscriptions(
            Long nodeId, PageQueryDTO query) {
        // 创建分页对象
        Page<SubscriptionWithBindingVO> page = new Page<>(
                query.getPage(), query.getPageSize());

        // 执行查询 - 使用自定义SQL需要用baseMapper
        IPage<SubscriptionWithBindingVO> result = baseMapper.selectSubscriptionsByNodeId(page,
                nodeId);

        // 转换结果
        return result.convert(subscriptionWithBindingConverter::toDTO);
    }

    @Override
    public void downloadNodeConfig(Long nodeId, HttpServletResponse response) {
        try {
            // 1. 获取节点信息
            Node node = this.getById(nodeId);
            if (node == null) {
                throw new BusinessException("节点不存在: " + nodeId);
            }

            // 2. 读取模板文件
            ClassPathResource resource = new ClassPathResource("templates/server-config.json");
            JsonNode configTemplate;
            try (InputStream inputStream = resource.getInputStream()) {
                configTemplate = objectMapper.readTree(inputStream);
            }

            // 3. 查询该节点关联的所有订阅用户
            List<Subscription> subscriptions = nodeSubscriptionRelationMapper.selectSubscriptionsByNodeIdList(nodeId);

            // 4. 根据节点协议类型填充用户配置
            if (configTemplate instanceof ObjectNode) {
                ObjectNode config = (ObjectNode) configTemplate;
                JsonNode inbounds = config.get("inbounds");

                if (inbounds != null && inbounds.isArray()) {
                    for (JsonNode inbound : inbounds) {
                        if (inbound instanceof ObjectNode) {
                            ObjectNode inboundNode = (ObjectNode) inbound;
                            String type = inboundNode.has("type") ? inboundNode.get("type").asText() : "";

                            // 为每种协议类型填充用户列表
                            ArrayNode users = objectMapper.createArrayNode();
                            for (Subscription subscription : subscriptions) {
                                ObjectNode user = createUserConfig(type, subscription);
                                if (user != null) {
                                    users.add(user);
                                }
                            }

                            // 替换模板中的users字段
                            if (users.size() > 0) {
                                inboundNode.set("users", users);
                            }

                            // 如果是 VLESS 协议，替换 Reality 配置
                            if ("vless".equalsIgnoreCase(type)) {
                                JsonNode tls = inboundNode.get("tls");
                                if (tls instanceof ObjectNode) {
                                    ObjectNode tlsNode = (ObjectNode) tls;
                                    JsonNode reality = tlsNode.get("reality");
                                    if (reality instanceof ObjectNode) {
                                        ObjectNode realityNode = (ObjectNode) reality;
                                        // 使用配置中的 Reality 私钥
                                        realityNode.put("private_key", realityConfig.getPrivateKey());
                                        // 更新 short_id（如果需要的话）
                                        ArrayNode shortIds = objectMapper.createArrayNode();
                                        shortIds.add(realityConfig.getShortId());
                                        realityNode.set("short_id", shortIds);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. 设置响应头
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            String filename = node.getName() + "-server-config.json";
            response.setHeader("Content-Disposition",
                "attachment; filename=\"" + new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1) + "\"");

            // 6. 写入响应
            try (OutputStream outputStream = response.getOutputStream()) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, configTemplate);
            }

            log.info("Downloaded node config for node: {}, name: {}", nodeId, node.getName());

        } catch (IOException e) {
            log.error("Failed to download node config for node: {}", nodeId, e);
            throw new BusinessException("下载配置文件失败: " + e.getMessage());
        }
    }

    @Override
    public void downloadNodeDockerComposeConfig(Long nodeId, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 1. 获取节点信息
            Node node = this.getById(nodeId);
            if (node == null) {
                throw new BusinessException("节点不存在: " + nodeId);
            }

            // 2. 读取模板文件
            ClassPathResource resource = new ClassPathResource("templates/docker-compose.yaml");
            String template;
            try (InputStream inputStream = resource.getInputStream()) {
                template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }

            // 3. 使用 HttpUtil 动态获取 baseUrl，支持反向代理
            String apiBaseUrl = HttpUtil.getBaseUrl(request);
            log.debug("Docker Compose 配置 API baseUrl: {}", apiBaseUrl);

            // 4. 替换模板中的占位符
            String dockerComposeContent = template
                .replace("{{NODE_NAME}}", node.getName())
                .replace("{{DOMAIN}}", node.getDomain() != null ? node.getDomain() : node.getIpAddress())
                .replace("${API_BASE_URL}", apiBaseUrl)
                .replace("${API_KEY}", apiServiceSecret);

            // 4. 设置响应头
            response.setContentType("text/yaml");
            response.setCharacterEncoding("UTF-8");
            String filename = node.getName() + "-docker-compose.yaml";
            response.setHeader("Content-Disposition",
                "attachment; filename=\"" + new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1) + "\"");

            // 5. 写入响应
            try (OutputStream outputStream = response.getOutputStream()) {
                outputStream.write(dockerComposeContent.getBytes(StandardCharsets.UTF_8));
            }

            log.info("Downloaded docker-compose config for node: {}, name: {}", nodeId, node.getName());

        } catch (IOException e) {
            log.error("Failed to download docker-compose config for node: {}", nodeId, e);
            throw new BusinessException("下载 Docker Compose 配置文件失败: " + e.getMessage());
        }
    }

    /**
     * 根据协议类型和订阅信息创建用户配置
     */
    private ObjectNode createUserConfig(String protocolType, Subscription subscription) {
        ObjectNode user = objectMapper.createObjectNode();

        switch (protocolType.toLowerCase()) {
            case "hysteria2":
                user.put("name", subscription.getGroupName());
                user.put("password", subscription.getOrderNo()); // 使用订单号作为密码
                break;

            case "tuic":
                user.put("name", subscription.getGroupName());
                user.put("uuid", subscription.getOrderNo());
                user.put("password", generatePassword(subscription.getOrderNo()));
                break;

            case "trojan":
                user.put("name", subscription.getGroupName());
                user.put("password", subscription.getOrderNo());
                break;

            case "vless":
                user.put("name", subscription.getGroupName());
                user.put("uuid", subscription.getOrderNo());
                break;

            default:
                return null;
        }

        return user;
    }

    /**
     * 生成密码（简单实现，可根据需要调整）
     */
    private String generatePassword(String seed) {
        // 这里可以使用更复杂的密码生成逻辑
        return seed.substring(0, Math.min(32, seed.length()));
    }
}
