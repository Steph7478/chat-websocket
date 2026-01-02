package com.app.chat.handlers;

import com.app.chat.heartbeat.HeartbeatService;
import com.app.chat.heartbeat.IdleTimeoutMonitor;
import com.app.chat.helpers.HeartBeatHelper;
import com.app.chat.helpers.RateLimiterHelper;
import com.app.chat.services.BroadCastService;
import com.app.chat.services.ChatProcessor;
import com.app.security.session.SessionRegistry;
import com.app.security.utils.SecurityLogger;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class ChatHandler implements WebSocketHandler {

        private final SessionRegistry registry;
        private final ChatProcessor processor;
        private final BroadCastService broadcastService;
        private final RateLimiterHelper rateLimiterHelper;
        private final HeartBeatHelper heartBeatHelper;
        private final HeartbeatService heartbeatService;
        private final IdleTimeoutMonitor idleTimeoutMonitor;

        public ChatHandler(
                        SessionRegistry registry,
                        ChatProcessor processor,
                        BroadCastService broadcastService,
                        RateLimiterHelper rateLimiterHelper,
                        HeartBeatHelper heartBeatHelper,
                        HeartbeatService heartbeatService,
                        IdleTimeoutMonitor idleTimeoutMonitor) {

                this.registry = registry;
                this.processor = processor;
                this.broadcastService = broadcastService;
                this.rateLimiterHelper = rateLimiterHelper;
                this.heartBeatHelper = heartBeatHelper;
                this.heartbeatService = heartbeatService;
                this.idleTimeoutMonitor = idleTimeoutMonitor;
        }

        @Override
        public Mono<Void> handle(WebSocketSession session) {

                var attrs = session.getAttributes();

                String user = (String) attrs.get("userId");
                String fp = (String) attrs.get("fingerprint");
                String ip = (String) attrs.get("ip");
                Boolean authenticated = (Boolean) attrs.get("authenticated");

                if (user == null || fp == null || ip == null || !Boolean.TRUE.equals(authenticated)) {
                        SecurityLogger.logAnomaly(
                                        "WS_UNAUTHENTICATED_ACCESS",
                                        "unknown",
                                        ip,
                                        "Tentativa de conexão sem autenticação");
                        return session.close();
                }

                AtomicLong lastSeen = new AtomicLong(System.currentTimeMillis());

                Mono<Void> initial = registry.registerSession(user, fp, session)
                                .then(broadcastService.register(session))
                                .then(session.send(Mono.just(
                                                session.textMessage(
                                                                "{\"type\":\"SYSTEM\",\"payload\":\"🔒 Criptografia ponta-a-ponta ativa\"}"))))
                                .then(broadcastService.broadcastSystemMessage(user + " entrou"))
                                .doOnSuccess(v -> SecurityLogger.logAnomaly("CHAT_JOIN", user, ip, "Usuário entrou"));

                Mono<Void> receive = session.receive()
                                .filter(msg -> msg.getType() == WebSocketMessage.Type.TEXT)
                                .map(WebSocketMessage::getPayloadAsText)
                                .flatMap(payload -> {
                                        if (heartBeatHelper.isHeartbeat(payload)) {
                                                lastSeen.set(System.currentTimeMillis());
                                                return Mono.empty();
                                        }
                                        return rateLimiterHelper.apply(payload, session)
                                                        .doOnNext(v -> lastSeen.set(System.currentTimeMillis()))
                                                        .flatMap(v -> processor.processMessage(
                                                                        payload, session, user, fp, ip));
                                })
                                .then();

                Mono<Void> send = session.send(
                                heartbeatService.heartbeat(session)
                                                .takeUntilOther(session.closeStatus()));

                Mono<Void> idle = idleTimeoutMonitor.monitor(session, lastSeen, user, ip);

                Mono<Void> cleanup = broadcastService.broadcastSystemMessage(user + " saiu")
                                .then(processor.cleanupSession(user, fp))
                                .then(broadcastService.unregister(session));

                return initial
                                .then(Mono.when(send, receive, idle))
                                .then(cleanup);
        }
}
