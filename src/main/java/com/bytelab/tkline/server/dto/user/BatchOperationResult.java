package com.bytelab.tkline.server.dto.user;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.ArrayList;

/**
 * 批量操作结果DTO
 * 
 * @author Apex Tunnel Team
 * @since 2025-10-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchOperationResult {
    
    /**
     * 成功的用户名列表
     */
    private List<String> successList = new ArrayList<>();
    
    /**
     * 失败的详情列表
     */
    private List<BatchOperationError> errorList = new ArrayList<>();
    
    /**
     * 成功数量
     */
    private Integer successCount;
    
    /**
     * 失败数量
     */
    private Integer failureCount;
    
    /**
     * 批量操作错误详情
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchOperationError {
        /**
         * 用户名
         */
        private String username;
        
        /**
         * 错误信息
         */
        private String errorMessage;
    }
    
    /**
     * 添加成功记录
     */
    public void addSuccess(String username) {
        this.successList.add(username);
        this.successCount = this.successList.size();
    }
    
    /**
     * 添加失败记录
     */
    public void addError(String username, String errorMessage) {
        this.errorList.add(new BatchOperationError(username, errorMessage));
        this.failureCount = this.errorList.size();
    }
}

