package com.app.security.decorators;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.app.security.utils.SecurityLogger;

import reactor.core.publisher.Mono;

public class ConnectionIntegrityDecorator implements WebSocketHandler {

    private final WebSocketHandler delegate;

    public ConnectionIntegrityDecorator(WebSocketHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        Boolean authenticated = (Boolean) session.getAttributes().get("authenticated");

        if (!Boolean.TRUE.equals(authenticated))
            return delegate.handle(session);

        String savedIP = (String) session.getAttributes().get("ip");
        String savedUA = (String) session.getAttributes().get("userAgent");

        if (savedIP == null || savedUA == null) {

            SecurityLogger.logAnomaly(
                    "SESSION_STATE_INVALID",
                    (String) session.getAttributes().get("userId"),
                    "unknown",
                    "Sessão marcada como autenticada mas sem fingerprint");

            return session.close();
        }

        String currentIP = session.getHandshakeInfo().getRemoteAddress() != null
                ? session.getHandshakeInfo().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";

        String currentUA = session.getHandshakeInfo()
                .getHeaders()
                .getFirst("User-Agent");

        if (!savedIP.equals(currentIP) || !savedUA.equals(currentUA)) {

            String cid = (String) session.getAttributes().get("correlationId");

            SecurityLogger.logAnomaly(
                    "SESSION_INCONSISTENCY",
                    (String) session.getAttributes().get("userId"),
                    currentIP,
                    "IP/UA diferente do fingerprint | CID: " + cid);

            return session.close();
        }

        return delegate.handle(session);
    }

}
