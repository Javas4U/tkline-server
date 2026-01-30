package com.bytelab.tkline.server.dto.ruleprovider;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Rule Provider DTO
 */
@Data
public class RuleProviderDTO {

    private Long id;

    private String name;

    private String type;

    private String behavior;

    private String format;

    private String url;

    private String path;

    private Integer updateInterval;

    private String description;

    private String policy;

    private Integer status;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
