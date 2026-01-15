package com.bytelab.tkline.server.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bytelab.tkline.server.common.ApiResult;
import com.bytelab.tkline.server.dto.PageQueryDTO;
import com.bytelab.tkline.server.dto.node.NodeDTO;
import com.bytelab.tkline.server.dto.relation.NodeSubscriptionBindDTO;
import com.bytelab.tkline.server.dto.subscription.SubscriptionCreateDTO;
import com.bytelab.tkline.server.dto.subscription.SubscriptionDTO;
import com.bytelab.tkline.server.dto.subscription.SubscriptionQueryDTO;
import com.bytelab.tkline.server.dto.subscription.SubscriptionUpdateDTO;
import com.bytelab.tkline.server.entity.Subscription;
import com.bytelab.tkline.server.service.SubscriptionService;
import com.bytelab.tkline.server.service.util.QRCodeService;
import com.bytelab.tkline.server.util.HttpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订阅管理控制器
 * <p>
 * 包含订阅管理接口(需要认证)和订阅配置接口(公开)
 */
@Slf4j
@Tag(name = "订阅管理", description = "订阅管理和配置接口")
@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final QRCodeService qrCodeService;

    /**
     * 创建订阅
     */
    @PostMapping("/createSubscription")
    public ApiResult<Long> createSubscription(@RequestBody @Valid SubscriptionCreateDTO createDTO) {
        Long subscriptionId = subscriptionService.createSubscription(createDTO);
        return ApiResult.success(subscriptionId);
    }

    /**
     * 更新订阅
     */
    @PutMapping("/updateSubscription")
    public ApiResult<Void> updateSubscription(
            @RequestBody @Valid SubscriptionUpdateDTO updateDTO) {
        subscriptionService.updateSubscription(updateDTO);
        return ApiResult.success(null);
    }

    /**
     * 获取订阅详情
     */
    @GetMapping("/getSubscriptionDetail/{id}")
    public ApiResult<SubscriptionDTO> getSubscriptionDetail(@PathVariable Long id) {
        SubscriptionDTO dto = subscriptionService.getSubscriptionDetail(id);
        return ApiResult.success(dto);
    }

    /**
     * 获取订阅绑定的节点ID列表
     */
    @GetMapping("/getSubscriptionNodeIds/{id}")
    public ApiResult<java.util.List<Long>> getSubscriptionNodeIds(@PathVariable Long id) {
        return ApiResult.success(subscriptionService.getSubscriptionNodeIds(id));
    }

    /**
     * 分页查询订阅
     */
    @PostMapping("/pageSubscriptions")
    public ApiResult<IPage<SubscriptionDTO>> pageSubscriptions(
            @RequestBody SubscriptionQueryDTO query) {
        return ApiResult.success(subscriptionService.pageSubscriptions(query));
    }

    /**
     * 分页查询订阅绑定的节点
     */
    @PostMapping("/pageSubscriptionNodes")
    public ApiResult<IPage<NodeDTO>> pageSubscriptionNodes(
            @RequestParam Long subscriptionId, @RequestBody PageQueryDTO query) {
        return ApiResult.success(subscriptionService.pageSubscriptionNodes(subscriptionId, query));
    }

    /**
     * 绑定节点到订阅(带配置)
     */
    @PostMapping("/bindNodesWithConfig")
    public ApiResult<Void> bindNodesWithConfig(
            @RequestParam Long subscriptionId,
            @RequestBody List<NodeSubscriptionBindDTO> bindings) {
        subscriptionService.bindNodesWithConfig(subscriptionId, bindings);
        return ApiResult.success(null);
    }

    /**
     * 从订阅解绑节点
     */
    @PostMapping("/unbindNodes")
    public ApiResult<Void> unbindNodes(
            @RequestParam Long subscriptionId,
            @RequestBody List<Long> nodeIds) {
        subscriptionService.unbindNodes(subscriptionId, nodeIds);
        return ApiResult.success(null);
    }

    /**
     * 同步订阅节点绑定配置（支持新增、更新、删除）
     */
    @PostMapping("/syncNodeBindings")
    public ApiResult<Void> syncNodeBindings(
            @RequestParam Long subscriptionId,
            @RequestBody List<NodeSubscriptionBindDTO> bindings) {
        subscriptionService.syncNodeBindings(subscriptionId, bindings);
        return ApiResult.success(null);
    }

    /**
     * 获取订阅配置URL
     */
    @GetMapping("/getSubscriptionUrl")
    public ApiResult<SubscriptionConfigUrlDTO> getSubscriptionUrl(
            @RequestParam Long subscriptionId,
            @RequestParam(required = false) List<Long> nodeIds,
            HttpServletRequest request) {
        // 使用 HttpUtil 动态获取 baseUrl,支持反向代理
        String baseUrl = HttpUtil.getBaseUrl(request);

        String subscriptionUrl = subscriptionService.getSubscriptionConfigUrl(subscriptionId, nodeIds, baseUrl);

        SubscriptionConfigUrlDTO result = new SubscriptionConfigUrlDTO();
        result.setSubscriptionUrl(subscriptionUrl);

        // 生成二维码
        try {
            String qrCode = qrCodeService.generateQRCodeDataUri(subscriptionUrl);
            result.setQrCode(qrCode);
        } catch (Exception e) {
            // 二维码生成失败不影响主流程,记录日志即可
            result.setQrCode("");
        }

        return ApiResult.success(result);
    }

    /**
     * 订阅配置URL返回DTO
     */
    public static class SubscriptionConfigUrlDTO {
        private String subscriptionUrl;
        private String qrCode;

        public String getSubscriptionUrl() {
            return subscriptionUrl;
        }

        public void setSubscriptionUrl(String subscriptionUrl) {
            this.subscriptionUrl = subscriptionUrl;
        }

        public String getQrCode() {
            return qrCode;
        }

        public void setQrCode(String qrCode) {
            this.qrCode = qrCode;
        }
    }

    // ========== 公开接口(无需认证) ==========

    /**
     * 获取订阅配置
     * <p>
     * 公开接口,根据orderNo获取订阅配置
     * 自动识别User-Agent返回对应格式(Clash YAML或Karing JSON)
     *
     * @param orderNo   订单号/订阅编号
     * @param nodeIds   可选节点ID列表(逗号分隔),用于只返回指定节点
     * @param userAgent 客户端User-Agent(自动获取)
     * @param format    可选格式参数(json/yaml),用于强制指定格式
     * @param request   HTTP请求对象,用于动态获取baseUrl
     * @return 配置文件内容
     */
    @GetMapping("/config")
    @Operation(summary = "获取订阅配置", description = "客户端获取订阅配置,后端根据User-Agent自动识别返回Clash或Karing格式,需要orderNo验证")
    public ResponseEntity<String> getSubscriptionConfig(
            @Parameter(description = "订单号/订阅编号", required = true) @RequestParam String orderNo,
            @Parameter(description = "可选节点ID列表(逗号分隔)") @RequestParam(required = false) String nodeIds,
            @Parameter(description = "客户端User-Agent(自动获取)") @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @Parameter(description = "可选格式参数(json/yaml),用于强制指定格式") @RequestParam(required = false) String format,
            HttpServletRequest request) {

        log.info("订阅配置请求: orderNo={}, nodeIds={}, userAgent={}, format={}", orderNo, nodeIds, userAgent, format);

        try {
            String config;
            String contentType;
            boolean isBrowser = isBrowser(userAgent);

            Subscription subscription = this.subscriptionService.getOne(new LambdaQueryWrapper<Subscription>().eq(Subscription::getOrderNo, orderNo));
            String filename = subscription.getGroupName();

            // 使用 HttpUtil 动态获取 baseUrl,支持反向代理
            String baseUrl = HttpUtil.getBaseUrl(request);
            log.debug("订阅配置 baseUrl: {}", baseUrl);

            // 解析nodeIds参数
            List<Long> nodeIdList = null;
            if (nodeIds != null && !nodeIds.isEmpty()) {
                nodeIdList = Arrays.stream(nodeIds.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
            }

            // 客户端及格式判断逻辑
            String clientType = detectClientType(userAgent);

            // 格式判断优先级: 1.显式指定format参数 2.UA识别 3.默认JSON
            if ("yaml".equalsIgnoreCase(format) || "clash".equalsIgnoreCase(clientType)) {
                // Clash 客户端或强制 YAML
                config = subscriptionService.generateYamlConfig(orderNo, nodeIdList, baseUrl);
                contentType = "text/yaml";
                filename = filename + ".yaml";
            } else if ("json".equalsIgnoreCase(format) || "karing".equalsIgnoreCase(clientType)
                    || "sing-box".equalsIgnoreCase(clientType)) {
                // Karing/Sing-Box 客户端或强制 JSON
                config = subscriptionService.generateJsonConfig(orderNo, nodeIdList, baseUrl);
                contentType = "application/json";
                filename = filename + ".json";
            } else if ("base64".equalsIgnoreCase(format) || "v2ray".equalsIgnoreCase(clientType)
                    || "shadowsocks".equalsIgnoreCase(clientType) || "shadowrocket".equalsIgnoreCase(clientType)) {
                // V2Ray/Shadowsocks/Shadowrocket 客户端或强制 Base64
                config = subscriptionService.generateBase64Config(orderNo, nodeIdList, baseUrl);
                contentType = "text/plain";
                filename = filename + ".txt";
            } else if (isBrowser) {
                // 浏览器访问默认返回 JSON (便于查看)
                config = subscriptionService.generateJsonConfig(orderNo, nodeIdList, baseUrl);
                contentType = "application/json";
                filename = filename + ".json";
            } else {
                // 其他未知客户端默认返回 Base64 (兼容性最好)
                config = subscriptionService.generateBase64Config(orderNo, nodeIdList, baseUrl);
                contentType = "text/plain";
                filename = filename + ".txt";
            }

            log.info("订阅配置生成成功: orderNo={}, nodeCount={}, contentType={}, configSize={}",
                    orderNo, nodeIdList != null ? nodeIdList.size() : "all", contentType, config.length());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType + ";charset=utf-8")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .header("profile-update-interval", "24")
                    .header("subscription-userinfo", "upload=0; download=0; total=10737418240; expire=2147483647")
                    .body(config);

        } catch (Exception e) {
            log.error("订阅配置生成失败: orderNo={}, 异常: {}", orderNo, e.getMessage(), e);
            String errorMessage = "配置生成失败: " + e.getMessage();
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(errorMessage);
        }
    }

    /**
     * 判断是否为浏览器User-Agent
     */
    private boolean isBrowser(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return false;
        }
        String ua = userAgent.toLowerCase();
        return ua.contains("mozilla") || ua.contains("chrome") || ua.contains("safari") || ua.contains("edge");
    }

    /**
     * 根据 User-Agent 识别客户端类型
     */
    private String detectClientType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "unknown";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("karing"))
            return "karing";
        if (ua.contains("clash") || ua.contains("stash") || ua.contains("surfboard"))
            return "clash";
        if (ua.contains("sing-box") || ua.contains("nekobox"))
            return "sing-box";
        if (ua.contains("v2ray") || ua.contains("v2fly"))
            return "v2ray";
        if (ua.contains("shadowsocks"))
            return "shadowsocks";
        if (ua.contains("shadowrocket"))
            return "shadowrocket";
        return "unknown";
    }
}
