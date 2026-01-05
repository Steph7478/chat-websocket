package com.app.chat.services;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
        String payload = String.format("{\"type\":\"SYSTEM\",\"payload\":\"%s\"}", text);
        return broadcast(payload);
    }

    public Mono<Void> broadcastUserList() {
        return Mono.defer(() -> {
            List<String> userList = sessions.stream()
                    .map(s -> (String) s.getAttributes().get("userId"))
                    .distinct()
                    .toList();

            String payloadNames = String.join(",", userList);
            String payload = String.format("{\"type\":\"USER_LIST\",\"payload\":\"%s\"}", payloadNames);
            return broadcast(payload);
        });
    }

    private Mono<Void> broadcast(String payload) {
        return Flux.fromIterable(sessions)
                .filter(WebSocketSession::isOpen)
                .flatMap(session -> session.send(Mono.just(session.textMessage(payload)))
                        .onErrorResume(e -> Mono.empty()))
                .then();
    }
}