package com.app.security.auth.jwt.services;

import com.app.security.auth.jwt.key.KeyProvider;
import com.nimbusds.jose.*;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.Date;

@Component
public class JwtService {

    public Mono<String> generate(String userId) {
        return Mono.fromCallable(() -> {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(userId)
                    .issueTime(new Date())
                    .expirationTime(Date.from(Instant.now().plusSeconds(900)))
                    .build();

            SignedJWT jwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .keyID(KeyProvider.getCurrentKid())
                            .build(),
                    claims);

            jwt.sign(KeyProvider.getSigner());
            return jwt.serialize();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> validateAndGetUser(String token) {
        return Mono.fromCallable(() -> {
            SignedJWT jwt = SignedJWT.parse(token);
            String kid = jwt.getHeader().getKeyID();
            JWSVerifier verifier = KeyProvider.getVerifier(kid);

            if (!jwt.verify(verifier))
                throw new RuntimeException("Invalid JWT signature");

            if (jwt.getJWTClaimsSet().getExpirationTime().before(new Date()))
                throw new RuntimeException("JWT expired");

            return jwt.getJWTClaimsSet().getSubject();
        })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> Mono.empty());
    }
}