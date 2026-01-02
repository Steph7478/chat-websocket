package com.app.security.ratelimiter.services;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.app.security.ratelimiter.config.RRateLimiterConfig;

import reactor.core.publisher.Mono;

@Component
public class RateLimiterMessages {

    private final RRateLimiterConfig rateLimiterService;

    public RateLimiterMessages(RRateLimiterConfig rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    public Mono<Boolean> canSendMessage(WebSocketSession session) {

        String ip = session.getHandshakeInfo()
                .getRemoteAddress()
                .getAddress()
                .getHostAddress();

        return rateLimiterService.isAllowed(ip)
                .flatMap(allowed -> allowed ? Mono.just(true)
                        : session.send(
                                Mono.just(session.textMessage(
                                        "{\"type\":\"SYSTEM\",\"payload\":\"Você está enviando mensagens muito rápido\"}")))
                                .thenReturn(false));
    }
}
