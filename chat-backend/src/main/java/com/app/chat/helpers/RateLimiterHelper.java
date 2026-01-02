package com.app.chat.helpers;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.app.security.ratelimiter.services.RateLimiterMessages;

import reactor.core.publisher.Mono;

@Component
public class RateLimiterHelper {

    private final RateLimiterMessages rateLimiter;

    public RateLimiterHelper(RateLimiterMessages rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    public Mono<Boolean> apply(String payload, WebSocketSession session) {
        if (!shouldRateLimit(payload))
            return Mono.just(true);

        return rateLimiter.canSendMessage(session)
                .filter(Boolean::booleanValue);
    }

    public boolean shouldRateLimit(String payload) {
        return payload.contains("\"TEXT\"")
                || payload.contains("\"ENCRYPTED_MSG\"");
    }
}
