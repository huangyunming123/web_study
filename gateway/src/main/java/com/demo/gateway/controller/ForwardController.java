package com.demo.gateway.controller;

import com.demo.gateway.common.R;
import com.demo.gateway.config.GatewayProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

/**
 * 教学纯转发网关控制器
 * 拦截所有请求并根据前缀路由规则无损转发到目标服务
 */
@Slf4j
@RestController
public class ForwardController {

    private final GatewayProperties gatewayProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 过滤 Hop-by-hop 协议头，避免在 HTTP 转发链中导致协议冲突
    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailers",
            "transfer-encoding",
            "upgrade",
            "host",
            "content-length"
    );

    public ForwardController(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
        // 配置 RestTemplate：连接超时与读取超时各 10 秒
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);
        this.restTemplate = new RestTemplate(factory);
        // 关闭默认错误处理，当下游返回 4xx/5xx 时不抛异常，原样透传状态码与响应体
        this.restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }
        });
    }

    @RequestMapping("/**")
    public void forward(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getRequestURI();

        // 1. 根据请求路径前缀匹配目标微服务路由
        GatewayProperties.Route matchedRoute = gatewayProperties.getRoutes().stream()
                .filter(route -> path.startsWith(route.getPrefix()))
                .findFirst()
                .orElse(null);

        // 2. 未找到匹配前缀，响应 404 统一返回体
        if (matchedRoute == null) {
            log.warn("网关路由未找到: path={}", path);
            writeJsonResponse(response, HttpStatus.NOT_FOUND.value(), R.fail(404, "route not found"));
            return;
        }

        // 3. 完整拼接目标 URL：目标 uri + 原始 path + 原始 query string
        String targetUrl = matchedRoute.getUri() + path;
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isBlank()) {
            targetUrl += "?" + queryString;
        }

        // 4. 读取原始请求 Body（严格使用 byte[]，避免字符串转换破坏编码和格式）
        byte[] requestBody = request.getInputStream().readAllBytes();

        // 5. 复制并透传原始请求 Headers（包括 Authorization 鉴权头）
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase())) {
                Enumeration<String> values = request.getHeaders(name);
                while (values.hasMoreElements()) {
                    headers.add(name, values.nextElement());
                }
            }
        }

        // 6. 执行转发调用
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        RequestEntity<byte[]> requestEntity = new RequestEntity<>(requestBody, headers, method, URI.create(targetUrl));

        try {
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(requestEntity, byte[].class);

            // 7. 回写下游状态码
            response.setStatus(responseEntity.getStatusCode().value());

            // 8. 回写下游响应头 (透传 Content-Type 并排除逐跳头)
            responseEntity.getHeaders().forEach((headerName, headerValues) -> {
                if (!HOP_BY_HOP_HEADERS.contains(headerName.toLowerCase())) {
                    for (String headerValue : headerValues) {
                        response.addHeader(headerName, headerValue);
                    }
                }
            });

            // 9. 回写下游响应体 byte[]
            byte[] responseBody = responseEntity.getBody();
            if (responseBody != null && responseBody.length > 0) {
                response.getOutputStream().write(responseBody);
                response.getOutputStream().flush();
            }
        } catch (RestClientException e) {
            // 下游服务不可达 (连接拒绝、超时等，ResourceAccessException 为其子类)，返回 502 统一返回体
            log.error("下游服务不可达: targetUrl={}, error={}", targetUrl, e.getMessage());
            writeJsonResponse(response, HttpStatus.BAD_GATEWAY.value(), R.fail(502, "service unavailable: " + e.getMessage()));
        }
    }

    /**
     * 将统一返回体序列化为 JSON 并写回响应
     */
    private void writeJsonResponse(HttpServletResponse response, int statusCode, R<?> result) throws IOException {
        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(result));
        response.getWriter().flush();
    }
}
