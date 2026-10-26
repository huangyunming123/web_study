package com.demo.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 鉴权服务主入口
 * 端口: 8081
 * Knife4j 文档: http://localhost:8081/doc.html
 */
@SpringBootApplication
@MapperScan("com.demo.auth.mapper")
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
