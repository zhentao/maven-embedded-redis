package com.zhentao.redis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import redis.clients.jedis.JedisPoolConfig;

import com.zhentao.redis.dao.BidHistoryDao;
import com.zhentao.redis.dao.BidHistoryDaoRedis;

@Configuration
@PropertySource(value = { "file:${runtime.properties}" })
public class RedisConfig {
    @Value("${redis.server}")
    private String redisServer;

    @Value("${redis.max.total}")
    private int maxTotal;
    @Value("${redis.idle}")
    private int idle;

    @Value("${redis.port}")
    private int port;

    @Value("${redis.max.wait.millis}")
    private long maxWaitMillis;

    @Value("${expiration.in.seconds}")
    private int expirationInSeconds;

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        // search local properties last by default
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public RedisConnectionFactory jedisConnectionFactory() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxIdle(idle);
        poolConfig.setMinIdle(idle);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMaxWaitMillis(maxWaitMillis);

        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(poolConfig);
        jedisConnectionFactory.setPort(port);

        return jedisConnectionFactory;
    }

    @Bean
    public StringRedisTemplate redisTemplate() {
        StringRedisTemplate redisTemplate = new StringRedisTemplate(jedisConnectionFactory());
        return redisTemplate;
    }

    @Bean
    public BidHistoryDao bidHistoryDao() {
        return new BidHistoryDaoRedis(redisTemplate(), expirationInSeconds);
    }
}
