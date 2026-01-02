package com.app.security.filters;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class HstsWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if ("https".equals(exchange.getRequest().getURI().getScheme())) {
            exchange.getResponse()
                    .getHeaders()
                    .set("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
        }
        return chain.filter(exchange);
    }
}
