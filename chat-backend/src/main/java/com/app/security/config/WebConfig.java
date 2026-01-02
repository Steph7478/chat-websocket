package com.app.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

import com.app.chat.handlers.ChatHandler;
import com.app.security.auth.jwt.decorators.JwtWebSocketHandlerDecorator;
import com.app.security.auth.jwt.services.JwtService;
import com.app.security.decorators.ConnectionIntegrityDecorator;

import java.util.Map;

@Configuration
public class WebConfig {

    private final ChatHandler chatHandler;
    private final JwtService jwtService;

    public WebConfig(ChatHandler chatHandler, JwtService jwtService) {
        this.chatHandler = chatHandler;
        this.jwtService = jwtService;
    }

    @Bean
    public HandlerMapping handlerMapping() {
        WebSocketHandler jwtHandler = new JwtWebSocketHandlerDecorator(chatHandler, jwtService);
        WebSocketHandler secureHandler = new ConnectionIntegrityDecorator(jwtHandler);

        return new SimpleUrlHandlerMapping(
                Map.of("/chat", secureHandler),
                -1);
    }
}
