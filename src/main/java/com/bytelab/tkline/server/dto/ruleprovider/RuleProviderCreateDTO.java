package com.bytelab.tkline.server.dto.ruleprovider;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Rule Provider 创建 DTO
 */
@Data
public class RuleProviderCreateDTO {

    @NotBlank(message = "规则名称不能为空")
    private String name;

    @NotBlank(message = "规则类型不能为空")
    private String type;

    @NotBlank(message = "行为类型不能为空")
    private String behavior;

    @NotBlank(message = "格式类型不能为空")
    private String format;

    @NotBlank(message = "规则源URL不能为空")
    private String url;

    @NotBlank(message = "本地存储路径不能为空")
    private String path;

    @NotNull(message = "更新间隔不能为空")
    private Integer updateInterval;

    private String description;

    @NotBlank(message = "规则策略不能为空")
    private String policy;

    @NotNull(message = "启用状态不能为空")
    private Integer status;

    private Integer sortOrder;
}
