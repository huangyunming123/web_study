package com.demo.biz.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Redisson 分布式锁配置类
 * 教学要点:
 * 1. Redisson 封装了 Redis 分布式锁的底层 Lua 脚本，实现了可重入加锁、看门狗自动续期(Watchdog)及释放锁逻辑。
 * 2. 此处读取项目统一的 Redis 连接配置，装配为单机 RedissonClient。
 */
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = "redis://" + host + ":" + port;

        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(address)
                .setConnectionMinimumIdleSize(4)
                .setConnectionPoolSize(16)
                .setTimeout(3000)
                .setConnectTimeout(5000);

        // 如果配置了有效密码则进行认证 (排除占位符)
        if (StringUtils.hasText(password) && !password.startsWith("YOUR_")) {
            serverConfig.setPassword(password);
        }

        return Redisson.create(config);
    }
}
