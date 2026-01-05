package com.app.chat.heartbeat;

import com.app.security.utils.SecurityLogger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class IdleTimeoutMonitor {

        private static final Duration CHECK = Duration.ofSeconds(5);
        private static final long TIMEOUT_MS = 60_000;

        public Mono<Void> monitor(
                        WebSocketSession session,
                        AtomicLong lastSeen,
                        String user,
                        String ip) {
                return Flux.interval(CHECK)
                                .takeUntilOther(session.closeStatus())
                                .filter(t -> System.currentTimeMillis() - lastSeen.get() > TIMEOUT_MS)
                                .next()
                                .doOnNext(t -> SecurityLogger.logAnomaly(
                                                "WS_IDLE_TIMEOUT", user, ip, "Inatividade excedida"))
                                .flatMap(t -> session.isOpen()
                                                ? session.close()
                                                : Mono.empty())
                                .then();
        }

        public void remove(String sessionId) {
        }
}
