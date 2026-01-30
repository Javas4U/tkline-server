package com.bytelab.tkline.server.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bytelab.tkline.server.common.ApiResult;
import com.bytelab.tkline.server.dto.ruleprovider.RuleProviderCreateDTO;
import com.bytelab.tkline.server.dto.ruleprovider.RuleProviderDTO;
import com.bytelab.tkline.server.dto.ruleprovider.RuleProviderQueryDTO;
import com.bytelab.tkline.server.dto.ruleprovider.RuleProviderUpdateDTO;
import com.bytelab.tkline.server.service.RuleProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Rule Provider 管理控制器
 */
@Slf4j
@Tag(name = "规则提供者管理", description = "Clash Rule Provider 配置管理接口")
@RestController
@RequestMapping("/api/rule-provider")
@RequiredArgsConstructor
public class RuleProviderController {

    private final RuleProviderService ruleProviderService;

    /**
     * 创建规则提供者
     */
    @PostMapping("/create")
    @Operation(summary = "创建规则提供者")
    public ApiResult<Long> createRuleProvider(@RequestBody @Valid RuleProviderCreateDTO createDTO) {
        Long id = ruleProviderService.createRuleProvider(createDTO);
        return ApiResult.success(id);
    }

    /**
     * 更新规则提供者
     */
    @PutMapping("/update")
    @Operation(summary = "更新规则提供者")
    public ApiResult<Void> updateRuleProvider(@RequestBody @Valid RuleProviderUpdateDTO updateDTO) {
        ruleProviderService.updateRuleProvider(updateDTO);
        return ApiResult.success(null);
    }

    /**
     * 获取规则提供者详情
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取规则提供者详情")
    public ApiResult<RuleProviderDTO> getRuleProviderDetail(@PathVariable Long id) {
        RuleProviderDTO dto = ruleProviderService.getRuleProviderDetail(id);
        return ApiResult.success(dto);
    }

    /**
     * 分页查询规则提供者
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询规则提供者")
    public ApiResult<IPage<RuleProviderDTO>> pageRuleProviders(@RequestBody RuleProviderQueryDTO query) {
        return ApiResult.success(ruleProviderService.pageRuleProviders(query));
    }

    /**
     * 删除规则提供者
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除规则提供者")
    public ApiResult<Void> deleteRuleProvider(@PathVariable Long id) {
        ruleProviderService.deleteRuleProvider(id);
        return ApiResult.success(null);
    }

    /**
     * 批量删除规则提供者
     */
    @DeleteMapping("/batch-delete")
    @Operation(summary = "批量删除规则提供者")
    public ApiResult<Void> batchDeleteRuleProviders(@RequestBody List<Long> ids) {
        ruleProviderService.batchDeleteRuleProviders(ids);
        return ApiResult.success(null);
    }

    /**
     * 获取所有启用的规则提供者
     */
    @GetMapping("/enabled")
    @Operation(summary = "获取所有启用的规则提供者")
    public ApiResult<List<RuleProviderDTO>> getEnabledRuleProviders() {
        return ApiResult.success(
            ruleProviderService.getEnabledRuleProviders()
                .stream()
                .map(rp -> {
                    RuleProviderDTO dto = new RuleProviderDTO();
                    dto.setId(rp.getId());
                    dto.setName(rp.getName());
                    dto.setType(rp.getType());
                    dto.setBehavior(rp.getBehavior());
                    dto.setFormat(rp.getFormat());
                    dto.setUrl(rp.getUrl());
                    dto.setPath(rp.getPath());
                    dto.setUpdateInterval(rp.getUpdateInterval());
                    dto.setDescription(rp.getDescription());
                    dto.setPolicy(rp.getPolicy());
                    dto.setStatus(rp.getStatus());
                    dto.setSortOrder(rp.getSortOrder());
                    return dto;
                })
                .toList()
        );
    }
}
