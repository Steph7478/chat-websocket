package com.app.security.decorators;

import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;

import com.app.security.utils.SecurityLogger;

import reactor.core.publisher.Mono;

public final class HttpAuditDecorator {

    private HttpAuditDecorator() {
    }

    public static <T> Mono<ResponseEntity<T>> audit(
            Mono<ResponseEntity<T>> original,
            ServerWebExchange exchange,
            String event,
            String user) {
        return original
                .doOnSuccess(res -> {
                    if (res == null)
                        return;

                    String ip = exchange.getRequest().getRemoteAddress() != null
                            ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                            : "unknown";

                    String cid = exchange.getAttribute("correlationId");

                    SecurityLogger.logAnomaly(
                            event,
                            user,
                            ip,
                            "HTTP event | CID=" + cid + " | status=" + res.getStatusCode());
                });
    }
}
