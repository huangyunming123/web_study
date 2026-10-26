package com.demo.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类 (基于 JJWT 0.12.x API)
 * 注: 生产环境中 jwt.secret 与 jwt.expire-hours 应配置在分布式配置中心 (如 Nacos / Apollo)
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:mall-demo-teaching-jwt-secret-key-must-be-at-least-256-bits-long-2026!}")
    private String secret;

    @Value("${jwt.expire-hours:24}")
    private long expireHours;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        // 使用 HS256 算法，要求密钥长度至少 256 位 (32 字节)
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return Token 字符串
     */
    public String generate(Long userId, String username) {
        long expireMillis = expireHours * 3600 * 1000L;
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireMillis);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析 JWT Token 获取 Claims 载荷
     *
     * @param token JWT 字符串
     * @return Claims 载荷
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证 Token 是否合法且未过期
     *
     * @param token JWT 字符串
     * @return 是否有效
     */
    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 校验失败: token={}, reason={}", token, e.getMessage());
            return false;
        }
    }
}
