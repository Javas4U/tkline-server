package com.bytelab.tkline.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bytelab.tkline.server.dto.PageQueryDTO;
import com.bytelab.tkline.server.dto.node.*;

import com.bytelab.tkline.server.dto.subscription.SubscriptionDTO;
import com.bytelab.tkline.server.entity.Node;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * 节点服务接口
 */
public interface NodeService extends IService<Node> {

    /**
     * 创建节点
     *
     * @param createDTO 创建请求
     * @return 节点ID
     */
    Long createNode(NodeCreateDTO createDTO);

    /**
     * 获取节点详情
     *
     * @param id 节点ID
     * @return 节点详情
     */
    NodeDTO getNodeDetail(Long id);

    /**
     * 分页查询节点
     * 
     * @param query 查询条件
     * @return 节点列表
     */
    IPage<NodeDTO> pageNodes(NodeQueryDTO query);

    /**
     * 处理节点心跳
     *
     * @param heartbeatDTO 心跳信息
     */
    void heartbeat(NodeHeartbeatDTO heartbeatDTO);

    /**
     * 分页查询节点绑定的订阅
     *
     * @param nodeId 节点ID
     * @param query  查询条件
     * @return 订阅列表
     */
    IPage<SubscriptionDTO> pageNodeSubscriptions(Long nodeId, PageQueryDTO query);

    void updateNode(@Valid NodeUpdateDTO updateDTO);

    /**
     * 下载节点配置文件
     * 根据server-config.json模板生成配置文件，并根据订阅组绑定关系填充users配置项
     *
     * @param nodeId   节点ID
     * @param response HTTP响应
     */
    void downloadNodeConfig(Long nodeId, HttpServletResponse response);

    /**
     * 下载节点 Docker Compose 配置文件
     * 根据 docker-compose.yaml 模板生成部署配置文件
     *
     * @param nodeId   节点ID
     * @param response HTTP响应
     */
    void downloadNodeDockerComposeConfig(Long nodeId, HttpServletResponse response);
}
