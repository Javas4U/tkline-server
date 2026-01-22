package com.bytelab.tkline.server.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.bytelab.tkline.server.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * 处理审计字段的自动填充（使用用户名而非用户ID）
 * <p>
 * 注意：此类不需要 @Component 注解，在 MybatisPlusConfig 中通过 @Bean 方式注册
 * 更新日期：2025-11-03（改用 SecurityUtils 获取用户信息）
 */
@Slf4j
public class AuditMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");
        
        // 获取当前用户名
        String currentUsername = getCurrentUsername();
        LocalDateTime now = LocalDateTime.now();
        
        // 填充创建相关字段
        this.strictInsertFill(metaObject, "createBy", String.class, currentUsername);
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateBy", String.class, currentUsername);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
    }

    /**
     * 更新时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");
        
        // 获取当前用户名
        String currentUsername = getCurrentUsername();
        LocalDateTime now = LocalDateTime.now();
        
        // 填充更新相关字段
        this.strictUpdateFill(metaObject, "updateBy", String.class, currentUsername);
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, now);
    }

    /**
     * 获取当前用户名
     * 从 Spring Security 的 SecurityContext 获取，如果未登录则返回"system"
     */
    private String getCurrentUsername() {
        String username = SecurityUtils.getCurrentUsername();
        // 如果未登录或无法获取用户名，返回"system"作为默认值
        return username != null ? username : "system";
    }
}
