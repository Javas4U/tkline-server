package com.bytelab.tkline.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson 序列化配置
 * <p>
 * 主要解决：
 * 1. Long 类型 ID 超出 JavaScript 安全整数范围（Number.MAX_SAFE_INTEGER = 2^53 - 1）
 * 2. 雪花算法生成的 19 位 Long ID 在前端会丢失精度
 * <p>
 * 解决方案：全局配置将 Long 和 long 类型序列化为 String
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();

        // 注册自定义模块：将 Long 类型序列化为 String
        SimpleModule simpleModule = new SimpleModule();
        
        // Long 类型（包装类）序列化为 String
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        
        // long 类型（基本类型）序列化为 String
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);

        objectMapper.registerModule(simpleModule);
        
        // 注册 Java 8 时间模块（如果需要）
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper;
    }
}

