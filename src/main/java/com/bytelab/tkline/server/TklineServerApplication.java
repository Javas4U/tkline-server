package com.bytelab.tkline.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

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
        SpringApplication.run(TklineServerApplication.class, args);
        System.out.println("\n=================================================");
        System.out.println("TKLine Server 启动成功!");
        System.out.println("Swagger UI: http://localhost:8081/swagger-ui.html");
        System.out.println("API Docs: http://localhost:8081/v3/api-docs");
        System.out.println("=================================================\n");
    }

}
