package com.bytelab.tkline.server.advice;

import com.bytelab.tkline.server.annotation.Decrypt;
import com.bytelab.tkline.server.service.core.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * 请求体自动解密处理器
 * <p>
 * 功能：
 * - 拦截所有 @RequestBody 参数
 * - 扫描DTO字段上的 @Decrypt 注解
 * - 自动解密标记字段（使用RSA私钥）
 * - 返回解密后的对象给Controller
 * <p>
 * 使用方式：
 * <pre>
 * {@code
 * // 1. DTO定义
 * public class CreateUserRequest {
 *     private String keyId;      // RSA密钥ID
 *     
 *     @Decrypt  // 标记需要解密
 *     private String email;      // 前端RSA加密的密文
 *     
 *     @Decrypt
 *     private String password;   // 前端RSA加密的密文
 * }
 * 
 * // 2. Controller使用
 * @PostMapping("/create")
 * public ApiResult create(@RequestBody CreateUserRequest request) {
 *     // request.email 已经自动解密为明文 ✅
 *     // request.password 已经自动解密为明文 ✅
 *     user.setEmail(request.getEmail());
 * }
 * }
 * </pre>
 * 
 * 工作流程：
 * 1. Controller接收JSON数据
 * 2. 反序列化为DTO对象
 * 3. DecryptRequestBodyAdvice拦截
 * 4. 扫描所有字段的@Decrypt注解
 * 5. 获取keyId字段的值
 * 6. 使用私钥解密标记字段
 * 7. 返回解密后的对象
 * 8. Controller收到的对象中加密字段已是明文
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class DecryptRequestBodyAdvice extends RequestBodyAdviceAdapter {

    private final SecurityService securityService;

    @Override
    public boolean supports(@NonNull MethodParameter methodParameter, 
                           @NonNull Type targetType, 
                           @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        // 支持所有 @RequestBody 参数
        return true;
    }

    @Override
    @NonNull
    public Object afterBodyRead(@NonNull Object body, 
                               @NonNull HttpInputMessage inputMessage, 
                               @NonNull MethodParameter parameter, 
                               @NonNull Type targetType, 
                               @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        
        try {
            // 解密DTO中的加密字段
            decryptFields(body);
        } catch (Exception e) {
            log.error("请求体自动解密失败", e);
            throw new RuntimeException("数据解密失败：" + e.getMessage());
        }

        return body;
    }

    /**
     * 解密对象中所有标记了@Decrypt的字段
     */
    private void decryptFields(Object obj) throws Exception {
        Class<?> clazz = obj.getClass();
        
        // 获取keyId字段的值（用于解密）
        String keyId = getKeyId(obj, clazz);
        
        if (!StringUtils.hasText(keyId)) {
            // 如果没有keyId，检查是否有需要解密的字段
            boolean hasDecryptFields = hasDecryptAnnotation(clazz);
            if (hasDecryptFields) {
                log.warn("对象包含@Decrypt字段但缺少keyId，无法解密，类：{}", clazz.getSimpleName());
            }
            return;
        }
        
        // 遍历所有字段，解密标记了@Decrypt的字段
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Decrypt.class)) {
                decryptField(obj, field, keyId);
            }
        }
    }

    /**
     * 解密单个字段
     */
    private void decryptField(Object obj, Field field, String keyId) throws Exception {
        field.setAccessible(true);
        
        // 获取加密值
        Object value = field.get(obj);
        
        if (value == null) {
            log.debug("字段值为null，跳过解密，字段：{}", field.getName());
            return;
        }
        
        if (!(value instanceof String)) {
            log.warn("@Decrypt注解只支持String类型字段，字段：{}，类型：{}", 
                    field.getName(), value.getClass().getSimpleName());
            return;
        }
        
        String encryptedValue = (String) value;
        
        if (!StringUtils.hasText(encryptedValue)) {
            log.debug("字段值为空，跳过解密，字段：{}", field.getName());
            return;
        }
        
        // 检查是否必须解密
        Decrypt annotation = field.getAnnotation(Decrypt.class);
        boolean required = annotation.required();
        
        try {
            // 使用RSA私钥解密
            String decryptedValue = securityService.decryptData(keyId, encryptedValue);
            
            // 设置解密后的值
            field.set(obj, decryptedValue);
            
            log.debug("字段自动解密成功，字段：{}，类：{}", 
                    field.getName(), obj.getClass().getSimpleName());
            
        } catch (Exception e) {
            if (required) {
                log.error("字段解密失败，字段：{}，keyId：{}", field.getName(), keyId, e);
                throw new RuntimeException("字段解密失败：" + field.getName());
            } else {
                log.warn("字段解密失败但非必需，保持原值，字段：{}", field.getName());
            }
        }
    }

    /**
     * 获取keyId字段的值
     */
    private String getKeyId(Object obj, Class<?> clazz) throws Exception {
        try {
            Field keyIdField = null;
            
            // 先查找@Decrypt注解中指定的keyIdField
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Decrypt.class)) {
                    Decrypt annotation = field.getAnnotation(Decrypt.class);
                    String keyIdFieldName = annotation.keyIdField();
                    keyIdField = clazz.getDeclaredField(keyIdFieldName);
                    break;
                }
            }
            
            // 如果没找到，使用默认的"keyId"字段
            if (keyIdField == null) {
                keyIdField = clazz.getDeclaredField("keyId");
            }
            
            keyIdField.setAccessible(true);
            Object value = keyIdField.get(obj);
            
            return value != null ? value.toString() : null;
            
        } catch (NoSuchFieldException e) {
            log.debug("对象中没有keyId字段，类：{}", clazz.getSimpleName());
            return null;
        }
    }

    /**
     * 检查类中是否有@Decrypt注解
     */
    private boolean hasDecryptAnnotation(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Decrypt.class)) {
                return true;
            }
        }
        return false;
    }
}

