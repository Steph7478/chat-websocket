package com.app.security.ratelimiter.filters;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.app.security.ratelimiter.config.RRateLimiterConfig;

import reactor.core.publisher.Mono;

@Component
public class RateLimiterFilter implements WebFilter {

    private final RRateLimiterConfig rateLimiterService;

    public RateLimiterFilter(RRateLimiterConfig rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String fetchDest = exchange.getRequest()
                .getHeaders()
                .getFirst("Sec-Fetch-Dest");

        if (!"document".equalsIgnoreCase(fetchDest)) {
            return chain.filter(exchange);
        }

        String ip = exchange.getRequest()
                .getRemoteAddress()
                .getAddress()
                .getHostAddress();

        return rateLimiterService.isAllowed(ip)
                .flatMap(allowed -> {
                    if (!allowed) {
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        return exchange.getResponse().setComplete();
                    }
                    return chain.filter(exchange);
                });
    }
}
