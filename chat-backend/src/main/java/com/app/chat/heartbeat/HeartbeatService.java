package com.app.chat.heartbeat;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Component
public class HeartbeatService {

    private static final Duration HEARTBEAT_INTERVAL = Duration.ofSeconds(20);

    public Flux<WebSocketMessage> heartbeat(WebSocketSession session) {
        return Flux.interval(HEARTBEAT_INTERVAL)
                .map(i -> session.textMessage(
                        "{\"type\":\"HEARTBEAT\"}"));
    }
}
