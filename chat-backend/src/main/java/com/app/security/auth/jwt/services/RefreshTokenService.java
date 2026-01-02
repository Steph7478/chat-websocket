package com.app.security.auth.jwt.services;

import org.springframework.stereotype.Component;

import com.app.security.utils.SecurityLogger;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RefreshTokenService {

    private final Map<String, String> tokenStore = new ConcurrentHashMap<>();
    private final Map<String, String> tokenFamily = new ConcurrentHashMap<>();

    public Mono<String> create(String userId) {
        return Mono.fromCallable(() -> {
            String token = UUID.randomUUID().toString();
            tokenStore.put(token, userId);
            return token;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> rotate(String oldToken) {
        return Mono.fromCallable(() -> {
            String userId = tokenStore.remove(oldToken);

            if (tokenFamily.containsKey(oldToken)) {
                String compromisedUser = tokenFamily.get(oldToken);
                SecurityLogger.logCritical("TOKEN_REUSE",
                        "Token reutilizado detectado para usuário " + compromisedUser);
                revokeAllSync(compromisedUser);
                throw new RuntimeException("Token reuse detected! Revoking all sessions.");
            }

            if (userId == null) {
                SecurityLogger.logAnomaly("INVALID_REFRESH_TOKEN", null, "unknown",
                        "Refresh token inválido: " + oldToken.substring(0, 5) + "...");
                throw new RuntimeException("Invalid refresh token");
            }

            String newToken = UUID.randomUUID().toString();
            tokenStore.put(newToken, userId);
            tokenFamily.put(oldToken, userId);

            return newToken;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> validate(String token) {
        return Mono.fromCallable(() -> {
            String userId = tokenStore.get(token);
            if (userId == null)
                throw new RuntimeException("Invalid refresh token");
            return userId;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> revokeAll(String userId) {
        return Mono.fromRunnable(() -> revokeAllSync(userId))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    private void revokeAllSync(String userId) {
        tokenStore.entrySet().removeIf(entry -> entry.getValue().equals(userId));
        tokenFamily.entrySet().removeIf(entry -> entry.getValue().equals(userId));
    }
}