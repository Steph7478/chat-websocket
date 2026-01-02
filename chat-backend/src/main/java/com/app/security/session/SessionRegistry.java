package com.app.security.session;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

    private final Map<String, Map<String, WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public Mono<Void> registerSession(String userId, String fingerprint, WebSocketSession session) {
        return Mono.fromRunnable(() -> {
            sessions.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                    .put(fingerprint, session);
        }).then();
    }

    public Mono<Boolean> isValid(String userId, String fingerprint) {
        return Mono.fromSupplier(() -> {
            Map<String, WebSocketSession> userSessions = sessions.get(userId);
            return userSessions != null && userSessions.containsKey(fingerprint);
        });
    }

    public Mono<WebSocketSession> getSession(String userId, String fingerprint) {
        return Mono.fromSupplier(() -> {
            Map<String, WebSocketSession> userSessions = sessions.get(userId);
            if (userSessions == null)
                return null;
            WebSocketSession session = userSessions.get(fingerprint);
            return (session != null && session.isOpen()) ? session : null;
        });
    }

    public Flux<WebSocketSession> getSessionsForUser(String userId) {
        return Flux.defer(() -> {
            Map<String, WebSocketSession> userSessions = sessions.get(userId);
            if (userSessions == null)
                return Flux.empty();

            return Flux.fromIterable(userSessions.values())
                    .filter(WebSocketSession::isOpen);
        });
    }

    public Flux<WebSocketSession> getAllSessions() {
        return Flux.fromIterable(sessions.values())
                .flatMap(map -> Flux.fromIterable(map.values()))
                .filter(WebSocketSession::isOpen);
    }

    public Mono<Void> removeSession(String userId, String fingerprint) {
        return Mono.fromRunnable(() -> {
            Map<String, WebSocketSession> userSessions = sessions.get(userId);
            if (userSessions != null) {
                userSessions.remove(fingerprint);
                if (userSessions.isEmpty()) {
                    sessions.remove(userId);
                }
            }
        }).then();
    }
}