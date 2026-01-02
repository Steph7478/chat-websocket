package com.app.security.auth.jwt.decorators;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.app.security.auth.jwt.services.JwtService;
import com.app.security.session.Fingerprint;
import com.app.security.utils.SecurityLogger;

import reactor.core.publisher.Mono;

public class JwtWebSocketHandlerDecorator implements WebSocketHandler {

    private final WebSocketHandler delegate;
    private final JwtService jwtService;

    public JwtWebSocketHandlerDecorator(
            WebSocketHandler delegate,
            JwtService jwtService) {

        this.delegate = delegate;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        var authCookie = session.getHandshakeInfo()
                .getCookies()
                .getFirst("__Host-AUTH");

        if (authCookie == null) {
            SecurityLogger.logAnomaly("LOGIN_FAILED", null, "unknown", "Cookie __Host-AUTH ausente");
            return session.close();
        }

        return jwtService.validateAndGetUser(authCookie.getValue())
                .flatMap(user -> {
                    var ua = session.getHandshakeInfo()
                            .getHeaders()
                            .getFirst("User-Agent");

                    var ip = session.getHandshakeInfo().getRemoteAddress() != null
                            ? session.getHandshakeInfo().getRemoteAddress().getAddress().getHostAddress()
                            : "unknown";
                    return Fingerprint.generate(user, ua, ip)
                            .flatMap(fingerprint -> {
                                session.getAttributes().put("userId", user);
                                session.getAttributes().put("fingerprint", fingerprint);
                                session.getAttributes().put("userAgent", ua);
                                session.getAttributes().put("ip", ip);
                                session.getAttributes().put("authenticated", true);

                                SecurityLogger.logAnomaly("LOGIN", user, ip, "WebSocket conectado");
                                return delegate.handle(session);
                            });
                })
                .switchIfEmpty(Mono.defer(session::close));
    }
}
