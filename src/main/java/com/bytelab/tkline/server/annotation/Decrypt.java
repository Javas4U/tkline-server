package com.bytelab.tkline.server.annotation;

import java.lang.annotation.*;

/**
 * 自动解密注解（Controller层DTO字段）
 * 
 * 用于标记Controller接收的DTO字段需要自动解密前端传来的RSA密文
 * 
 * 使用场景：
 * 前端使用RSA公钥加密敏感数据后传给后端，后端自动解密
 * 
 * 当前状态：⚠️ 未实现自动解密
 * 需要配合AOP或RequestBodyAdvice实现自动解密功能
 * 
 * 使用示例：
 * <pre>
 * {@code
 * public class CreateUserRequest {
 *     private String username;
 *     
 *     @Decrypt  // 标记此字段需要解密
 *     private String email;  // 前端传来RSA加密的邮箱
 *     
 *     private String keyId;  // RSA密钥ID
 * }
 * 
 * // Controller接收后自动解密
 * @PostMapping("/create")
 * public ApiResult<UserInfoDTO> createUser(@RequestBody CreateUserRequest request) {
 *     // request.email 已经自动解密为明文 ✅
 *     user.setEmail(request.getEmail());
 * }
 * }
 * </pre>
 * 
 * 实现方式（待开发）：
 * 1. 创建RequestBodyAdvice拦截@RequestBody
 * 2. 扫描DTO字段上的@Decrypt注解
 * 3. 使用keyId字段的值获取私钥
 * 4. 自动解密标记字段
 * 5. 返回解密后的对象
 * 
 * 当前替代方案：
 * Service层手动调用 securityKeyService.decryptData()
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Decrypt {

    /**
     * 指定用于解密的密钥ID字段名
     * 默认为 "keyId"
     * 
     * @return 密钥ID字段名
     */
    String keyIdField() default "keyId";

    /**
     * 是否必须解密
     * 如果为true，解密失败将抛出异常
     * 如果为false，解密失败将保持原值
     * 
     * @return 是否必须解密
     */
    boolean required() default true;
}

