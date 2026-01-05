package com.app.chat.heartbeat;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HeartbeatService {

    private static final Duration HEARTBEAT_INTERVAL = Duration.ofSeconds(20);

    private final Map<String, Disposable> active = new ConcurrentHashMap<>();

    public Flux<WebSocketMessage> heartbeat(WebSocketSession session) {
        return Flux.interval(HEARTBEAT_INTERVAL)
                .map(i -> session.textMessage("{\"type\":\"HEARTBEAT\"}"))
                .doOnSubscribe(sub -> active.put(session.getId(), (Disposable) sub))
                .doFinally(sig -> active.remove(session.getId()));
    }

    public void stop(String sessionId) {
        Disposable d = active.remove(sessionId);
        if (d != null) {
            d.dispose();
        }
    }
}
