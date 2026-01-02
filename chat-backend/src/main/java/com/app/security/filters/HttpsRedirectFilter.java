package com.app.security.filters;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class HttpsRedirectFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (exchange.getRequest().getURI().getScheme().equals("http")) {
            var uri = exchange.getRequest().getURI();
            var httpsUri = uri.toString().replace("http://", "https://");
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.MOVED_PERMANENTLY);
            exchange.getResponse().getHeaders().setLocation(java.net.URI.create(httpsUri));
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }
}
