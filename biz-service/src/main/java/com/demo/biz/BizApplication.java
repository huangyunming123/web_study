package com.demo.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 业务服务主入口
 * 端口: 8082
 * Knife4j 文档: http://localhost:8082/doc.html
 */
@SpringBootApplication
@MapperScan("com.demo.biz.mapper")
public class BizApplication {

    public static void main(String[] args) {
        SpringApplication.run(BizApplication.class, args);
    }
}
