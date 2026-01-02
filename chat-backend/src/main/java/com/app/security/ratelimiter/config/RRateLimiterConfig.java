package com.app.security.ratelimiter.config;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class RRateLimiterConfig {

    private final RedissonClient client;
    private static final long LIMIT = 10L;
    private static final Duration DURATION = Duration.ofSeconds(10);
    private final ConcurrentMap<String, RRateLimiter> rateLimiters = new ConcurrentHashMap<>();

    public RRateLimiterConfig(RedissonClient client) {
        this.client = client;
    }

    public RRateLimiter getRateLimiter(String key) {
        return rateLimiters.computeIfAbsent(key, k -> {
            RRateLimiter limiter = client.getRateLimiter("ratelimiter:" + k);
            limiter.trySetRate(RateType.OVERALL, LIMIT, DURATION);
            return limiter;
        });
    }

    public Mono<Boolean> isAllowed(String ip) {
        RRateLimiter limiter = getRateLimiter(ip);
        return Mono.fromFuture(limiter.tryAcquireAsync(1).toCompletableFuture());
    }
}
