package com.app.chat.serializers;

import com.app.chat.dto.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class WebSocketSender {
    private final ObjectMapper mapper = new ObjectMapper();

    public Mono<Void> send(WebSocketSession session, ChatMessage msg) {
        return session.send(Mono.just(session.textMessage(toJson(msg))))
                .onErrorResume(e -> Mono.empty());
    }

    public Mono<Void> send(Flux<WebSocketSession> sessions, ChatMessage msg) {
        return sessions.filter(WebSocketSession::isOpen)
                .flatMap(s -> send(s, msg))
                .then()
                .onErrorResume(e -> Mono.empty());
    }

    private String toJson(ChatMessage msg) {
        try {
            return mapper.writeValueAsString(msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
