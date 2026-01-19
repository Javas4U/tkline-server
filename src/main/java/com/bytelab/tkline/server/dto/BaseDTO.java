package com.bytelab.tkline.server.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 基础DTO类
 * 包含通用的审计字段
 */
@Data
public abstract class BaseDTO {

    /**
     * 创建人ID
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改人ID
     */
    private String updateBy;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除状态：0-未删除，1-已删除
     */
    private Integer deleted;
}
