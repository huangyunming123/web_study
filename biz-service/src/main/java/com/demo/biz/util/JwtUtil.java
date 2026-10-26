package com.demo.biz.util;

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
 * JWT 工具类 (与 auth-service 保持完全一致的加解密逻辑)
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
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

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

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

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
