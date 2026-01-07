package com.bytelab.tkline.server.controller;

import com.bytelab.tkline.server.common.ApiResult;
import com.bytelab.tkline.server.dto.subscription.SubscriptionCreateDTO;
import com.bytelab.tkline.server.dto.subscription.SubscriptionDTO;
import com.bytelab.tkline.server.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订阅管理控制器
 */
@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

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
            @RequestBody @Valid com.bytelab.tkline.server.dto.subscription.SubscriptionUpdateDTO updateDTO) {
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
    public ApiResult<com.baomidou.mybatisplus.core.metadata.IPage<SubscriptionDTO>> pageSubscriptions(
            @RequestBody com.bytelab.tkline.server.dto.subscription.SubscriptionQueryDTO query) {
        return ApiResult.success(subscriptionService.pageSubscriptions(query));
    }

    /**
     * 分页查询订阅绑定的节点
     */
    @PostMapping("/pageSubscriptionNodes")
    public ApiResult<com.baomidou.mybatisplus.core.metadata.IPage<com.bytelab.tkline.server.dto.node.NodeDTO>> pageSubscriptionNodes(
            @RequestParam Long subscriptionId, @RequestBody com.bytelab.tkline.server.dto.PageQueryDTO query) {
        return ApiResult.success(subscriptionService.pageSubscriptionNodes(subscriptionId, query));
    }
}
