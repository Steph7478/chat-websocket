package com.app.chat.services;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BroadCastService {

    private final Set<WebSocketSession> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public Mono<Void> register(WebSocketSession session) {
        return Mono.fromRunnable(() -> sessions.add(session));
    }

    public Mono<Void> unregister(WebSocketSession session) {
        return Mono.fromRunnable(() -> sessions.remove(session));
    }

    public Mono<Void> broadcastSystemMessage(String text) {
        String payload = "{\"type\":\"SYSTEM\",\"payload\":\"" + text + "\"}";
        return broadcast(payload);
    }

    public Mono<Void> broadcastTextMessage(String text) {
        String payload = "{\"type\":\"TEXT\",\"payload\":\"" + text + "\"}";
        return broadcast(payload);
    }

    private Mono<Void> broadcast(String payload) {
        return Flux.fromIterable(sessions)
                .filter(WebSocketSession::isOpen)
                .flatMap(session -> session.send(Mono.just(session.textMessage(payload))))
                .then();
    }
}
