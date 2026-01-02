package com.app.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;

import com.app.security.auth.cookies.CookieFactory;
import com.app.security.auth.jwt.services.JwtService;
import com.app.security.auth.jwt.services.RefreshTokenService;
import com.app.security.decorators.HttpAuditDecorator;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<String>> login(
            @RequestParam String username,
            ServerWebExchange exchange) {

        Mono<ResponseEntity<String>> original = Mono.zip(
                jwtService.generate(username),
                refreshTokenService.create(username)).map(tuple -> {
                    exchange.getResponse().addCookie(
                            CookieFactory.createAuthCookie(tuple.getT1()));
                    exchange.getResponse().addCookie(
                            CookieFactory.createRefreshCookie(tuple.getT2()));

                    return ResponseEntity.ok("Autenticado com sucesso");
                });

        return HttpAuditDecorator.audit(
                original,
                exchange,
                "LOGIN_SUCCESS",
                username);
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<Map<String, String>>> check(ServerWebExchange exchange) {
        var authCookie = exchange.getRequest().getCookies().getFirst("__Host-AUTH");

        if (authCookie == null)
            return Mono.just(ResponseEntity.status(401).build());

        return jwtService.validateAndGetUser(authCookie.getValue())
                .map(user -> ResponseEntity.ok(Map.of("username", user)))
                .switchIfEmpty(
                        HttpAuditDecorator.audit(
                                Mono.just(ResponseEntity.status(401).build()),
                                exchange,
                                "AUTH_CHECK_FAILED",
                                "unknown"));

    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(ServerWebExchange exchange) {

        Mono<ResponseEntity<Void>> original = Mono.fromCallable(() -> {
            exchange.getResponse().addCookie(
                    CookieFactory.deleteCookie("__Host-AUTH"));
            exchange.getResponse().addCookie(
                    CookieFactory.deleteCookie("__Host-REFRESH"));

            return ResponseEntity.noContent().build();
        });

        return HttpAuditDecorator.audit(
                original,
                exchange,
                "LOGOUT",
                "unknown");
    }

}
