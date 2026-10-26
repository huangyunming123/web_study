package com.demo.biz.interceptor;

import com.demo.biz.common.R;
import com.demo.biz.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 业务服务统一鉴权拦截器
 *
 * 【组件选型理由说明 - HandlerInterceptor vs OncePerRequestFilter】：
 * 1. 业务契约天然契合：HandlerInterceptor 作为 Spring MVC 核心扩展点，天然支持结合 WebMvcConfigurer 进行白名单与路径模式匹配（excludePathPatterns），代码组织清晰；
 * 2. 上下文透传：通过 preHandle 可以在请求进入具体的 Controller 前完成拦截，通过后将解析出的 userId 直接绑定至 request.setAttribute("userId", userId)，下游 Controller 可直接获取；
 * 3. 教学心智模型清晰：相比底层 Servlet Filter，Spring 拦截器是教学演示 Spring Boot 控制层鉴权的标准模式。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String LOGIN_TOKEN_PREFIX = "login:token:";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1. 获取请求头 Authorization
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            reject(response, "未提供身份认证凭据 (Token)");
            return false;
        }

        // 2. 去除 "Bearer " 前缀获取原生 JWT
        String token = authHeader.substring(7).trim();

        // 3. 使用与 auth-service 相同 secret 验证签名及有效性
        if (!jwtUtil.validate(token)) {
            reject(response, "Token 无效或已过期，请重新登录");
            return false;
        }

        // 4. 解析 Token 获取 userId
        Claims claims = jwtUtil.parse(token);
        Object userIdObj = claims.get("userId");
        if (userIdObj == null) {
            reject(response, "Token 载荷缺失关键用户信息");
            return false;
        }

        Long userId = Long.valueOf(userIdObj.toString());

        // 5. 校验 Redis 中是否存在该 key，且与当前 token 相等 (单点/主动注销判定)
        String redisKey = LOGIN_TOKEN_PREFIX + userId;
        String redisToken = stringRedisTemplate.opsForValue().get(redisKey);
        if (redisToken == null || !redisToken.equals(token)) {
            reject(response, "登录状态已失效，请重新登录");
            return false;
        }

        // 6. 校验通过，把 userId 存入 request attribute 供后续 Controller/Service 使用
        request.setAttribute("userId", userId);
        request.setAttribute("username", claims.get("username", String.class));
        return true;
    }

    /**
     * 鉴权失败写回 401 统一响应体
     */
    private void reject(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        R<Void> fail = R.fail(401, msg);
        response.getWriter().write(objectMapper.writeValueAsString(fail));
        response.getWriter().flush();
    }
}
