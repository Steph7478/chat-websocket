package com.app.security.auth.jwt.services;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class SilentRefreshService {

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    public SilentRefreshService(RefreshTokenService refreshTokenService, JwtService jwtService) {
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
    }

    public static record TokenPair(String accessToken, String refreshToken) {
    }

    public Mono<TokenPair> refresh(String oldRefreshToken) {
        return refreshTokenService.validate(oldRefreshToken)
                .flatMap(userId -> Mono.zip(
                        jwtService.generate(userId),
                        refreshTokenService.rotate(oldRefreshToken))
                        .map(tuple -> new TokenPair(tuple.getT1(), tuple.getT2())))
                .onErrorResume(e -> Mono.error(new RuntimeException("Refresh failed", e)));
    }
}