package com.app.chat.services;

import com.app.chat.dto.ChatMessage;
import com.app.chat.serializers.WebSocketSender;
import com.app.security.session.PublicKeyRegistry;
import com.app.security.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ChatProcessor {

        private final SessionRegistry registry;
        private final PublicKeyRegistry publicKeyRegistry;
        private final WebSocketSender sender;
        private final ObjectMapper mapper = new ObjectMapper();

        public ChatProcessor(SessionRegistry registry, PublicKeyRegistry publicKeyRegistry,
                        WebSocketSender sender) {
                this.registry = registry;
                this.publicKeyRegistry = publicKeyRegistry;
                this.sender = sender;
        }

        public Mono<Void> processMessage(String payload, WebSocketSession s, String u, String f, String ip) {
                return Mono.fromCallable(() -> mapper.readValue(payload, ChatMessage.class))
                                .flatMap(msg -> {
                                        msg.setFrom(u);

                                        return switch (msg.getType()) {
                                                case "KEY_EXCHANGE" ->
                                                        handleKeyExchange(u, f, msg.getPublicKey(), s);
                                                case "GET_PUB_KEY" ->
                                                        handleGetPubKey(msg.getTo(), s);
                                                case "TEXT", "ENCRYPTED_MSG" ->
                                                        handleForwarding(msg, s);
                                                default -> Mono.empty();
                                        };
                                })
                                .onErrorResume(e -> Mono.empty());
        }

        private Mono<Void> handleKeyExchange(String u, String f, String key, WebSocketSession s) {
                return publicKeyRegistry.registerKey(u, f, key)
                                .then(sender.send(s,
                                                createMsg("KEY_EXCHANGE_ACK", "SYSTEM", u,
                                                                "Chave registrada com sucesso", null)));
        }

        private Mono<Void> handleGetPubKey(String to, WebSocketSession s) {
                return publicKeyRegistry.getAllKeysForUser(to)
                                .flatMapMany(keysMap -> Flux.fromIterable(keysMap.values()))
                                .next()
                                .flatMap(key -> sender.send(s, createMsg("PUB_KEY_RESPONSE", to, null, null, key)));
        }

        private Mono<Void> handleForwarding(ChatMessage msg, WebSocketSession s) {
                if ("TODOS".equals(msg.getTo())) {
                        Flux<WebSocketSession> targets = registry.getAllSessions()
                                        .filter(sess -> !sess.getId().equals(s.getId()));

                        return sender.send(targets, msg);
                }

                Flux<WebSocketSession> specificTargets = registry.getSessionsForUser(msg.getTo());

                return sender.send(specificTargets, msg);
        }

        private ChatMessage createMsg(String type, String from, String to, String payload, String pubKey) {
                ChatMessage m = new ChatMessage();
                m.setType(type);
                m.setFrom(from);
                m.setTo(to);
                m.setPayload(payload);
                m.setPublicKey(pubKey);
                return m;
        }

        public Mono<Void> cleanupSession(String u, String f) {
                return publicKeyRegistry.removeKeys(u, f)
                                .then(registry.removeSession(u, f));
        }
}
