package com.app.controllers;

import com.app.security.auth.jwt.services.JwtService;
import com.app.security.auth.jwt.services.RefreshTokenService;
import com.app.security.decorators.HttpAuditDecorator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private WebTestClient webTestClient;

    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private HttpAuditDecorator httpAuditDecorator;

    @InjectMocks
    private AuthController authController;

    private final String username = "testuser";

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(authController).build();
    }

    @Test
    void testLogin() {
        when(jwtService.generate(username)).thenReturn(Mono.just("jwt-token"));
        when(refreshTokenService.create(username)).thenReturn(Mono.just("refresh-token"));

        webTestClient.post()
                .uri(uri -> uri.path("/auth/login").queryParam("username", username).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Autenticado com sucesso")
                .consumeWith(resp -> {
                    var cookies = resp.getResponseHeaders().get(HttpHeaders.SET_COOKIE);
                    assert cookies != null;
                    assert cookies.stream().anyMatch(c -> c.contains("__Host-AUTH=jwt-token"));
                    assert cookies.stream().anyMatch(c -> c.contains("__Host-REFRESH=refresh-token"));
                });

    }

    @Test
    void testCheckAuthenticated() {
        when(jwtService.validateAndGetUser("jwt-token")).thenReturn(Mono.just(username));

        webTestClient.get()
                .uri("/auth/me")
                .cookie("__Host-AUTH", "jwt-token")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo(username);
    }

    @Test
    void testCheckUnauthorized() {
        webTestClient.get()
                .uri("/auth/me")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testLogout() {
        webTestClient.post()
                .uri("/auth/logout")
                .exchange()
                .expectStatus().isNoContent()
                .expectHeader().valueMatches(HttpHeaders.SET_COOKIE, ".*Max-Age=0.*");
    }
}