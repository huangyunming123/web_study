package com.demo.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j OpenAPI3 文档配置
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("商城教学项目 - 鉴权中心 API")
                        .version("1.0.0")
                        .description("提供用户登录、用户注册、Token合法性校验服务")
                        .contact(new Contact().name("Teacher").email("demo@example.com")));
    }
}
