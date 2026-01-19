package com.bytelab.tkline.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.core.env.Environment;

/**
 * TKLine Server Application
 * 主应用启动类
 */
@SpringBootApplication
@MapperScan("com.bytelab.tkline.server.mapper")
@EnableCaching
@EnableAsync
@EnableScheduling
public class TklineServerApplication {

    public static void main(String[] args) {
        var context = SpringApplication.run(TklineServerApplication.class, args);
        Environment env = context.getEnvironment();
        String port = env.getProperty("server.port", "8081");
        boolean swaggerEnabled = Boolean.parseBoolean(env.getProperty("springdoc.api-docs.enabled", "false"));

        System.out.println("\n=================================================");
        System.out.println("TKLine Server 启动成功!");
        System.out.println("应用端口: http://localhost:" + port);

        if (swaggerEnabled) {
            System.out.println("Swagger UI: http://localhost:" + port + "/swagger-ui.html");
            System.out.println("API Docs: http://localhost:" + port + "/v3/api-docs");
        }

        System.out.println("=================================================\n");
    }

}
