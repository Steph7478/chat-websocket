package com.app.security.auth.jwt.filters;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.app.security.auth.cookies.CookieFactory;
import com.app.security.auth.jwt.services.JwtService;
import com.app.security.auth.jwt.services.SilentRefreshService;

import reactor.core.publisher.Mono;

@Component
public class SilentRefreshHttpFilter implements WebFilter {

    private final SilentRefreshService silentRefreshService;
    private final JwtService jwtService;

    public SilentRefreshHttpFilter(SilentRefreshService silentRefreshService, JwtService jwtService) {
        this.silentRefreshService = silentRefreshService;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var authCookie = exchange.getRequest().getCookies().getFirst("__Host-AUTH");
        var refreshCookie = exchange.getRequest().getCookies().getFirst("__Host-REFRESH");

        if (authCookie != null && refreshCookie != null) {
            try {
                jwtService.validateAndGetUser(authCookie.getValue());
                return chain.filter(exchange);
            } catch (Exception e) {
                return silentRefreshService.refresh(refreshCookie.getValue())
                        .flatMap(pair -> {
                            exchange.getResponse().addCookie(
                                    CookieFactory.createAuthCookie(pair.accessToken()));
                            exchange.getResponse().addCookie(
                                    CookieFactory.createRefreshCookie(pair.refreshToken()));
                            return chain.filter(exchange);
                        })
                        .onErrorResume(err -> chain.filter(exchange));
            }
        }
        return chain.filter(exchange);
    }
}
