package com.bytelab.tkline.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bytelab.tkline.server.annotation.BCrypt;
import com.bytelab.tkline.server.handler.BCryptTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 * <p>
 * 字段加密说明：
 * - password: 使用BCrypt单向散列（不可逆，最安全）
 * - email, phone: 明文存储（需要查询和索引）
 * - id_card: 可选加密（使用@Encrypt注解 + EncryptTypeHandler）
 * <p>
 * 如需对某个字段启用加密，添加注解：
 * @Encrypt  或 @BCrypt
 * @TableField(value = "field_name", typeHandler = XxxTypeHandler.class)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户名（明文存储，需要查询）
     */
    @TableField("username")
    private String username;

    /**
     * 密码（BCrypt散列存储，不可逆）
     * <p>
     * 使用@BCrypt注解 + BCryptTypeHandler自动处理：
     * - 存储时：自动散列（明文 → 散列值）
     * - 读取时：直接返回散列值
     * - 验证时：BCryptTypeHandler.verify(明文, 散列值)
     * <p>
     * Service层使用：
     * user.setPassword("password123");  // 直接设置明文，框架自动散列！
     * <p>
     * 验证密码：
     * boolean isValid = BCryptTypeHandler.verify(
     *     userInput,           // 用户输入的明文
     *     user.getPassword()   // 数据库读取的散列值
     * );
     */
    @BCrypt
    @TableField(value = "password", typeHandler = BCryptTypeHandler.class)
    private String password;

    /**
     * 邮箱（明文存储，需要查询和索引）
     * <p>
     * 如需加密，修改为：
     * @Encrypt
     * @TableField(value = "email", typeHandler = EncryptTypeHandler.class)
     * 并将数据库字段类型改为TEXT
     */
    @TableField("email")
    private String email;

    /**
     * 手机号（明文存储，需要查询和索引）
     * <p>
     * 如需加密，修改为：
     * @Encrypt
     * @TableField(value = "phone", typeHandler = EncryptTypeHandler.class)
     * 并将数据库字段类型改为TEXT
     */
    @TableField("phone")
    private String phone;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 用户角色：SUPER_ADMIN-超级管理员，ADMIN-管理员，USER-普通用户
     * <p>
     * 角色等级（从高到低）：
     * - SUPER_ADMIN: 可以管理所有用户
     * - ADMIN: 可以管理普通管理员和普通用户
     * - USER: 普通用户，无管理权限
     */
    @TableField("role")
    private String role;

}
