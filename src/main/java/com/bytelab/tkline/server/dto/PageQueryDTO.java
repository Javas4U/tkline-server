package com.bytelab.tkline.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分页查询基类
 * 包含通用的分页参数和验证逻辑
 *
 * @author apex-tunnel
 */
@Data
public class PageQueryDTO {

    @Schema(description = "页码", example = "1", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "20", defaultValue = "20")
    private Integer pageSize = 20;

    @Schema(description = "名称(模糊查询)", example = "节点1")
    private String name;

    @Schema(description = "区域(模糊查询)", example = "香港")
    private String region;

    /**
     * 验证并修正分页参数
     */
    public void validate() {
        if (pageSize == null || pageSize <= 0) {
            pageSize = 20;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
    }

    /**
     * 计算SQL OFFSET
     *
     * @return offset值
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }

    /**
     * 获取页码（兼容page字段）
     */
    public Integer getPage() {
        return pageNum;
    }

    /**
     * 设置页码（兼容page字段）
     */
    public void setPage(Integer page) {
        this.pageNum = page;
    }
}
