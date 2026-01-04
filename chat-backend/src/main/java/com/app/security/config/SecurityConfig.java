package com.app.security.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    var config = new CorsConfiguration();
                    config.setAllowedOriginPatterns(List.of(
                            "http://localhost:*",
                            "https://localhost:*"));

                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
                    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));
                    config.setAllowCredentials(true);

                    return config;
                }))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/auth/**", "/chat", "/pages/**", "/*.css", "/*.js").permitAll()
                        .anyExchange().permitAll())
                .build();
    }
}
