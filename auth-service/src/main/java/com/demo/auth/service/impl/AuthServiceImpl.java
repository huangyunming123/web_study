package com.demo.auth.service.impl;

import com.demo.auth.common.BusinessException;
import com.demo.auth.dto.LoginDTO;
import com.demo.auth.dto.RegisterDTO;
import com.demo.auth.entity.SysUser;
import com.demo.auth.mapper.SysUserMapper;
import com.demo.auth.service.AuthService;
import com.demo.auth.util.JwtUtil;
import com.demo.auth.vo.LoginVO;
import com.demo.auth.vo.VerifyVO;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 鉴权服务实现类 (基于原生 MyBatis 框架操作数据库)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String LOGIN_TOKEN_PREFIX = "login:token:";

    @Override
    public Long register(RegisterDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new BusinessException(500, "用户名和密码不能为空");
        }

        // 1. 使用原生 MyBatis 查询用户名唯一性
        Long count = sysUserMapper.countByUsername(dto.getUsername().trim());
        if (count != null && count > 0) {
            throw new BusinessException(500, "用户名已存在");
        }

        // 2. 密码使用 BCrypt 强哈希加密
        String encodedPassword = passwordEncoder.encode(dto.getPassword().trim());

        SysUser user = SysUser.builder()
                .username(dto.getUsername().trim())
                .password(encodedPassword)
                .nickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname().trim() : dto.getUsername().trim())
                .createTime(LocalDateTime.now())
                .build();

        // 3. 执行原生 MyBatis 插入 (自动回填 user.id)
        sysUserMapper.insert(user);
        log.info("新用户注册成功: id={}, username={}", user.getId(), user.getUsername());
        return user.getId();
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        if (!StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 1. 使用原生 MyBatis 查询用户
        SysUser user = sysUserMapper.selectByUsername(dto.getUsername().trim());
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 2. 校验 BCrypt 密码
        if (!passwordEncoder.matches(dto.getPassword().trim(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 3. 生成 JWT (HS256)
        String token = jwtUtil.generate(user.getId(), user.getUsername());

        // 4. 写入 Redis：key = "login:token:{userId}", value = token, TTL 24h
        String redisKey = LOGIN_TOKEN_PREFIX + user.getId();
        stringRedisTemplate.opsForValue().set(redisKey, token, 24, TimeUnit.HOURS);
        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());

        return LoginVO.builder()
                .token(token)
                .userInfo(LoginVO.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .build())
                .build();
    }

    @Override
    public VerifyVO verify(String authHeader) {
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(401, "未提供有效的 Authorization 请求头");
        }

        String token = authHeader.substring(7).trim();
        // 校验 JWT 格式与签名
        if (!jwtUtil.validate(token)) {
            throw new BusinessException(401, "Token 签名无效或已过期");
        }

        Claims claims = jwtUtil.parse(token);
        Object userIdObj = claims.get("userId");
        if (userIdObj == null) {
            throw new BusinessException(401, "Token 载荷缺失用户信息");
        }

        Long userId = Long.valueOf(userIdObj.toString());
        String username = claims.get("username", String.class);

        // 查 Redis login:token:{userId} 存在且与当前 token 相等
        String redisKey = LOGIN_TOKEN_PREFIX + userId;
        String redisToken = stringRedisTemplate.opsForValue().get(redisKey);
        if (redisToken == null || !redisToken.equals(token)) {
            throw new BusinessException(401, "Token 已失效或在其他设备登录");
        }

        return VerifyVO.builder()
                .valid(true)
                .userId(userId)
                .username(username)
                .build();
    }
}
