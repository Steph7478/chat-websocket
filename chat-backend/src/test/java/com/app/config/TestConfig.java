package com.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.redisson.api.RedissonClient;
import static org.mockito.Mockito.mock;

@Configuration
public class TestConfig {

    @Bean
    @Primary
    public RedissonClient redissonClientMock() {
        return mock(RedissonClient.class);
    }
}
