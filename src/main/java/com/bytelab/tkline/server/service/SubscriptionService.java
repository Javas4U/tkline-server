package com.bytelab.tkline.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bytelab.tkline.server.dto.PageQueryDTO;
import com.bytelab.tkline.server.dto.node.NodeDTO;
import com.bytelab.tkline.server.dto.relation.NodeSubscriptionBindDTO;
import com.bytelab.tkline.server.dto.subscription.SubscriptionCreateDTO;
import com.bytelab.tkline.server.dto.subscription.SubscriptionDTO;
import com.bytelab.tkline.server.dto.subscription.SubscriptionQueryDTO;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 订阅服务接口
 */
public interface SubscriptionService {

    /**
     * 创建订阅
     *
     * @param createDTO 创建请求
     * @return 订阅ID
     */
    Long createSubscription(SubscriptionCreateDTO createDTO);

    /**
     * 获取订阅详情
     *
     * @param id 订阅ID
     * @return 订阅详情
     */
    SubscriptionDTO getSubscriptionDetail(Long id);

    /**
     * 分页查询订阅
     *
     * @param query 查询条件
     * @return 订阅列表
     */
    IPage<SubscriptionDTO> pageSubscriptions(SubscriptionQueryDTO query);

    /**
     * 更新订阅
     *
     * @param updateDTO 更新请求
     */
    void updateSubscription(com.bytelab.tkline.server.dto.subscription.SubscriptionUpdateDTO updateDTO);

    /**
     * 获取订阅绑定的节点ID列表
     *
     * @param subscriptionId 订阅ID
     * @return 节点ID列表
     */
    List<Long> getSubscriptionNodeIds(Long subscriptionId);

    /**
     * 分页查询订阅绑定的节点
     *
     * @param subscriptionId 订阅ID
     * @param query          查询条件
     * @return 节点列表
     */
    IPage<NodeDTO> pageSubscriptionNodes(Long subscriptionId, PageQueryDTO query);

    /**
     * 绑定节点到订阅(带配置)
     *
     * @param subscriptionId 订阅ID
     * @param bindings       节点绑定配置列表
     */
    void bindNodesWithConfig(Long subscriptionId, List<NodeSubscriptionBindDTO> bindings);

    /**
     * 从订阅解绑节点
     *
     * @param subscriptionId 订阅ID
     * @param nodeIds        节点ID列表
     */
    void unbindNodes(Long subscriptionId, List<Long> nodeIds);

    /**
     * 同步订阅节点绑定配置（支持新增、更新、删除）
     *
     * @param subscriptionId 订阅ID
     * @param bindings       节点绑定配置列表
     */
    void syncNodeBindings(Long subscriptionId, List<NodeSubscriptionBindDTO> bindings);

    /**
     * 获取订阅配置URL
     *
     * @param subscriptionId 订阅ID
     * @param nodeIds        节点ID列表(可选,用于筛选特定节点)
     * @param baseUrl        基础URL
     * @return 订阅配置URL
     */
    String getSubscriptionConfigUrl(Long subscriptionId, List<Long> nodeIds, String baseUrl);

    /**
     * 生成YAML格式配置(Clash)
     *
     * @param orderNo 订单号/订阅编号
     * @param nodeIds 可选节点ID列表,为null时返回所有节点
     * @param baseUrl 服务器基础URL
     * @return Clash YAML配置内容
     */
    String generateYamlConfig(String orderNo, List<Long> nodeIds, String baseUrl) throws UnsupportedEncodingException;

    /**
     * 生成JSON格式配置(Karing/Sing-Box)
     *
     * @param orderNo 订单号/订阅编号
     * @param nodeIds 可选节点ID列表,为null时返回所有节点
     * @param baseUrl 服务器基础URL
     * @return Karing/Sing-Box JSON配置内容
     */
    String generateJsonConfig(String orderNo, List<Long> nodeIdList, String baseUrl) throws UnsupportedEncodingException;

    /**
     * 生成Base64格式配置(V2Ray/Shadowsocks/通用)
     *
     * @param orderNo    订单号/订阅编号
     * @param nodeIdList 可选节点ID列表,为null时返回所有节点
     * @param baseUrl    服务器基础URL
     * @return Base64编码的配置内容
     */
    String generateBase64Config(String orderNo, List<Long> nodeIdList, String baseUrl);
}
