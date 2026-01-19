package com.bytelab.tkline.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytelab.tkline.server.converter.NodeConverter;
import com.bytelab.tkline.server.converter.SubscriptionConverter;
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
import com.bytelab.tkline.server.service.NodeService;
import com.bytelab.tkline.server.util.RealityKeyUtil;
import java.time.LocalDateTime;
import java.util.Map;

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
public class NodeServiceImpl extends ServiceImpl<NodeMapper, Node> implements NodeService {

    private final NodeConverter nodeConverter;

    private final SubscriptionConverter subscriptionConverter;

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
        node.setCreateBy("admin"); // TODO: 获取当前登录用户
        node.setUpdateBy("admin");

        // 3. 检查是否包含 vless 协议，如果包含则生成 Reality 密钥对
        if (containsVlessProtocol(createDTO.getProtocols())) {
            Map<String, String> realityKeys = RealityKeyUtil.generateRealityKeyPair();
            node.setRealityPublicKey(realityKeys.get("publicKey"));
            node.setRealityPrivateKey(realityKeys.get("privateKey"));
            log.info("Generated Reality keys for node: {}", createDTO.getName());
        }

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
        if (query.getIpAddress() != null && !query.getIpAddress().isEmpty()) {
            wrapper.eq(Node::getIpAddress, query.getIpAddress());
        }
        if (query.getRegion() != null && !query.getRegion().isEmpty()) {
            wrapper.like(Node::getRegion, query.getRegion());
        }
        // ignore status for now if type mismatch or handle if needed

        wrapper.orderByDesc(Node::getId);

        IPage<Node> result = this.page(page, wrapper);
        return result.convert(nodeConverter::toDTO);
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

        // 检查协议是否包含 vless
        boolean hadVless = containsVlessProtocol(existingNode.getProtocols());
        boolean hasVless = containsVlessProtocol(updateDTO.getProtocols());

        // 创建 UpdateWrapper 用于更新
        LambdaUpdateWrapper<Node> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Node::getId, updateDTO.getId());

        // 更新基本字段
        updateWrapper.set(Node::getName, updateDTO.getName())
                .set(Node::getIpAddress, updateDTO.getIpAddress())
                .set(Node::getPort, updateDTO.getPort())
                .set(Node::getRegion, updateDTO.getRegion())
                .set(Node::getDescription, updateDTO.getDescription())
                .set(Node::getProtocols, updateDTO.getProtocols())
                .set(Node::getUpstreamQuota, updateDTO.getUpstreamQuota())
                .set(Node::getDownstreamQuota, updateDTO.getDownstreamQuota())
                .set(Node::getUpdateBy, "admin"); // TODO: 当前用户

        // 根据协议变化处理 Reality 密钥
        // 如果之前没有 vless，现在有了，则生成新的 Reality 密钥对
        if (!hadVless && hasVless) {
            Map<String, String> realityKeys = RealityKeyUtil.generateRealityKeyPair();
            updateWrapper.set(Node::getRealityPublicKey, realityKeys.get("publicKey"))
                    .set(Node::getRealityPrivateKey, realityKeys.get("privateKey"));
            log.info("Generated new Reality keys for node: {}", updateDTO.getId());
        }
        // 如果之前有 vless，现在没有了，则清除 Reality 密钥
        else if (hadVless && !hasVless) {
            updateWrapper.set(Node::getRealityPublicKey, null)
                    .set(Node::getRealityPrivateKey, null);
            log.info("Removed Reality keys for node: {}", updateDTO.getId());
        }
        // 如果之前有 vless，现在还有，则不修改 Reality 密钥（保持原值）
        // 不需要在 updateWrapper 中设置这两个字段

        this.update(null, updateWrapper);
        log.info("Node updated: id={}", updateDTO.getId());
    }

    @Override
    public IPage<SubscriptionDTO> pageNodeSubscriptions(
            Long nodeId, PageQueryDTO query) {
        // 创建分页对象
        Page<Subscription> page = new Page<>(
                query.getPage(), query.getPageSize());

        // 执行查询 - 使用自定义SQL需要用baseMapper
        IPage<Subscription> result = baseMapper.selectSubscriptionsByNodeId(page,
                nodeId);

        // 转换结果
        return result.convert(subscriptionConverter::toDTO);
    }

    /**
     * 检查协议列表中是否包含 vless 协议
     *
     * @param protocols 协议列表 JSON 字符串，如 ["hy2","vless","vmess"]
     * @return true 如果包含 vless
     */
    private boolean containsVlessProtocol(String protocols) {
        if (protocols == null || protocols.isEmpty()) {
            return false;
        }
        // 简单检查是否包含 "vless" 字符串（忽略大小写）
        return protocols.toLowerCase().contains("vless");
    }
}
