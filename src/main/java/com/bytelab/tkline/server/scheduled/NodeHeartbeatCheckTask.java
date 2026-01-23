package com.bytelab.tkline.server.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bytelab.tkline.server.entity.Node;
import com.bytelab.tkline.server.service.NodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 节点健康检查定时任务
 * 主动检查 sing-box 节点服务器的健康状态
 * 通过访问 sing-box 的 Clash API 进行健康检查
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NodeHeartbeatCheckTask {

    private final NodeService nodeService;
    private final RestTemplate restTemplate;

    @Value("${node.health-check.timeout:5000}")
    private int healthCheckTimeout;

    @Value("${node.health-check.clash-api-port:9090}")
    private int clashApiPort;

    @Value("${node.health-check.clash-api-secret:}")
    private String clashApiSecret;

    /**
     * 定时执行节点健康检查
     * 执行频率由配置文件中的 node.health-check.check-cron 控制
     * 默认: 每3分钟执行一次 (0 *\/3 * * * ?)
     */
    @Scheduled(cron = "${node.health-check.check-cron:0 */3 * * * ?}")
    public void checkNodeHealth() {
        log.info("====== 开始执行: 节点健康检查 ======");

        try {
            // 获取所有节点
            List<Node> allNodes = nodeService.list(new LambdaQueryWrapper<Node>());

            if (allNodes.isEmpty()) {
                log.info("没有需要检查的节点");
                return;
            }

            int onlineCount = 0;
            int offlineCount = 0;
            LocalDateTime now = LocalDateTime.now();

            // 并发检查所有节点
            List<CompletableFuture<Void>> futures = allNodes.stream()
                .map(node -> CompletableFuture.runAsync(() -> checkSingleNode(node, now)))
                .toList();

            // 等待所有检查完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(healthCheckTimeout * allNodes.size() + 10000L, TimeUnit.MILLISECONDS);

            // 统计最终状态
            for (Node node : allNodes) {
                Node updated = nodeService.getById(node.getId());
                if (updated != null && updated.getStatus() != null && updated.getStatus() == 1) {
                    onlineCount++;
                } else {
                    offlineCount++;
                }
            }

            log.info("节点健康检查完成 - 在线: {}, 离线: {}, 总计: {}",
                onlineCount, offlineCount, allNodes.size());

        } catch (Exception e) {
            log.error("====== 节点健康检查异常 ======", e);
        }
    }

    /**
     * 检查单个节点的健康状态
     */
    private void checkSingleNode(Node node, LocalDateTime checkTime) {
        boolean isHealthy = false;
        String errorMessage = null;

        try {
            // 构建健康检查 URL
            // 优先使用域名，如果没有域名则使用IP地址
            String host = (node.getDomain() != null && !node.getDomain().isEmpty())
                ? node.getDomain()
                : node.getIpAddress();

            // sing-box Clash API 健康检查端点
            String healthCheckUrl = String.format("http://%s:%d/", host, clashApiPort);

            log.debug("检查节点: {} [{}] - URL: {}", node.getName(), node.getId(), healthCheckUrl);

            // 构建请求头，添加 Authorization
            HttpHeaders headers = new HttpHeaders();
            if (clashApiSecret != null && !clashApiSecret.isEmpty()) {
                headers.set("Authorization", "Bearer " + clashApiSecret);
            }
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 发送 HTTP GET 请求检查节点（带认证）
            restTemplate.exchange(new URI(healthCheckUrl), HttpMethod.GET, entity, String.class);

            // 如果请求成功（没有抛出异常），认为节点健康
            isHealthy = true;
            log.debug("节点 {} [{}] 健康检查成功", node.getName(), node.getId());

        } catch (Exception e) {
            errorMessage = e.getMessage();
            log.warn("节点 {} [{}] 健康检查失败: {}", node.getName(), node.getId(), errorMessage);
        }

        // 更新节点状态
        updateNodeStatus(node, isHealthy, checkTime);
    }

    /**
     * 更新节点状态
     */
    private void updateNodeStatus(Node node, boolean isHealthy, LocalDateTime checkTime) {
        Integer newStatus = isHealthy ? 1 : 0;
        Integer currentStatus = node.getStatus();
        boolean statusChanged = !newStatus.equals(currentStatus);

        // 更新节点状态和最后心跳时间
        node.setStatus(newStatus);
        if (isHealthy) {
            node.setLastHeartbeatTime(checkTime);
        }
        nodeService.updateById(node);

        // 如果状态发生变化，记录日志
        if (statusChanged) {
            log.info("节点状态变更: {} [{}] {} -> {}",
                node.getName(),
                node.getId(),
                currentStatus != null && currentStatus == 1 ? "在线" : "离线",
                isHealthy ? "在线" : "离线"
            );
        }
    }

    /**
     * 定期清理长时间离线的节点心跳记录（可选）
     * 执行频率由配置文件中的 node.health-check.cleanup-cron 控制
     * 默认: 每天凌晨2点 (0 0 2 * * ?)
     */
    @Scheduled(cron = "${node.health-check.cleanup-cron:0 0 2 * * ?}")
    public void cleanupOfflineNodes() {
        log.info("====== 开始执行: 清理离线节点状态 ======");

        try {
            LocalDateTime threshold = LocalDateTime.now().minusDays(7); // 7天未心跳
            List<Node> longOfflineNodes = nodeService.list(
                new LambdaQueryWrapper<Node>()
                    .lt(Node::getLastHeartbeatTime, threshold)
                    .eq(Node::getStatus, 1) // 仍然标记为在线
            );

            if (!longOfflineNodes.isEmpty()) {
                for (Node node : longOfflineNodes) {
                    node.setStatus(0); // 标记为离线
                    nodeService.updateById(node);
                    log.info("清理长时间离线节点: {} [{}], 最后心跳: {}",
                        node.getName(),
                        node.getId(),
                        node.getLastHeartbeatTime()
                    );
                }
                log.info("清理完成，共处理 {} 个长时间离线节点", longOfflineNodes.size());
            } else {
                log.info("没有需要清理的长时间离线节点");
            }

        } catch (Exception e) {
            log.error("====== 清理离线节点状态异常 ======", e);
        }
    }
}
