package com.bytelab.tkline.server.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bytelab.tkline.server.common.ApiResult;
import com.bytelab.tkline.server.dto.PageQueryDTO;
import com.bytelab.tkline.server.dto.node.NodeCreateDTO;
import com.bytelab.tkline.server.dto.node.NodeDTO;
import com.bytelab.tkline.server.dto.node.NodeHeartbeatDTO;
import com.bytelab.tkline.server.dto.node.NodeQueryDTO;
import com.bytelab.tkline.server.dto.node.NodeUpdateDTO;
import com.bytelab.tkline.server.dto.subscription.SubscriptionDTO;
import com.bytelab.tkline.server.service.NodeService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 节点管理控制器
 */
@RestController
@RequestMapping("/api/v1/node")
@RequiredArgsConstructor
public class NodeController {

    private final NodeService nodeService;

    /**
     * 创建节点
     */
    @PostMapping("/createNode")
    public ApiResult<Long> createNode(@RequestBody @Valid NodeCreateDTO createDTO) {
        Long nodeId = nodeService.createNode(createDTO);
        return ApiResult.success(nodeId);
    }

    /**
     * 获取节点详情
     */
    @GetMapping("/getNodeDetail/{id}")
    public ApiResult<NodeDTO> getNodeDetail(@PathVariable Long id) {
        NodeDTO nodeDTO = nodeService.getNodeDetail(id);
        return ApiResult.success(nodeDTO);
    }

    /**
     * 分页查询节点
     */
    @PostMapping("/pageNodes")
    public ApiResult<IPage<NodeDTO>> pageNodes(
            @RequestBody NodeQueryDTO query) {
        return ApiResult.success(nodeService.pageNodes(query));
    }

    @Operation(summary = "节点心跳")
    @PostMapping("/heartbeat")
    public ApiResult<Void> heartbeat(@RequestBody @Valid NodeHeartbeatDTO heartbeatDTO) {
        nodeService.heartbeat(heartbeatDTO);
        return ApiResult.success();
    }

    /**
     * 更新节点
     */
    @PostMapping("/updateNode")
    @Operation(summary = "更新节点")
    public ApiResult<Void> updateNode(@RequestBody @Valid NodeUpdateDTO updateDTO) {
        nodeService.updateNode(updateDTO);
        return ApiResult.success();
    }

    /**
     * 分页查询节点绑定的订阅
     */
    @PostMapping("/pageNodeSubscriptions")
    public ApiResult<IPage<SubscriptionDTO>> pageNodeSubscriptions(
            @RequestParam Long nodeId, @RequestBody PageQueryDTO query) {
        return ApiResult.success(nodeService.pageNodeSubscriptions(nodeId, query));
    }
}
