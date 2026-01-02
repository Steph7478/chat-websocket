package com.app.security.session;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PublicKeyRegistry {

    private final Map<String, Map<String, String>> keys = new ConcurrentHashMap<>();

    public Mono<Void> registerKey(String userId, String fingerprint, String publicKey) {
        return Mono.fromRunnable(() -> keys.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                .put(fingerprint, publicKey));
    }

    public Mono<String> getKey(String userId, String fingerprint) {
        return Mono.fromSupplier(() -> {
            Map<String, String> userKeys = keys.get(userId);
            return (userKeys != null) ? userKeys.get(fingerprint) : null;
        });
    }

    public Mono<Map<String, String>> getAllKeysForUser(String userId) {
        return Mono.fromSupplier(() -> {
            Map<String, String> userKeys = keys.get(userId);
            if (userKeys != null && !userKeys.isEmpty()) {
                return userKeys;
            }
            return Map.of();
        });
    }

    public Mono<Void> removeKeys(String userId, String fingerprint) {
        return Mono.fromRunnable(() -> {
            Map<String, String> userKeys = keys.get(userId);
            if (userKeys != null) {
                userKeys.remove(fingerprint);
                if (userKeys.isEmpty()) {
                    keys.remove(userId);
                }
            }
        });
    }
}
