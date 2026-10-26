package com.demo.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关路由映射配置
 * 绑定 application.yml 中的 gateway 节点
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {

    /**
     * 路由规则列表
     */
    private List<Route> routes = new ArrayList<>();

    @Data
    public static class Route {
        /**
         * 匹配的前缀路径 (如 /auth 或 /api)
         */
        private String prefix;

        /**
         * 目标微服务基地址 (如 http://localhost:8081)
         */
        private String uri;
    }
}
