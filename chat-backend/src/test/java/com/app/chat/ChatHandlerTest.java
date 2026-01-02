package com.app.chat;

import com.app.chat.handlers.ChatHandler;
import com.app.chat.heartbeat.HeartbeatService;
import com.app.chat.heartbeat.IdleTimeoutMonitor;
import com.app.chat.helpers.HeartBeatHelper;
import com.app.chat.helpers.RateLimiterHelper;
import com.app.chat.services.BroadCastService;
import com.app.chat.services.ChatProcessor;
import com.app.security.session.SessionRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatHandlerTest {

    @Mock
    private SessionRegistry registry;
    @Mock
    private ChatProcessor processor;
    @Mock
    private BroadCastService broadcastService;
    @Mock
    private RateLimiterHelper rateLimiterHelper;
    @Mock
    private HeartBeatHelper heartBeatHelper;
    @Mock
    private HeartbeatService heartbeatService;
    @Mock
    private IdleTimeoutMonitor idleTimeoutMonitor;

    @InjectMocks
    private ChatHandler handler;

    @Test
    void testHandleAuthenticatedUser() {
        WebSocketSession session = mock(WebSocketSession.class);

        WebSocketMessage mockMessage = mock(WebSocketMessage.class);
        lenient().when(session.textMessage(anyString())).thenReturn(mockMessage);

        when(session.getAttributes()).thenReturn(new HashMap<>() {
            {
                put("userId", "user1");
                put("fingerprint", "fp1");
                put("ip", "127.0.0.1");
                put("authenticated", true);
            }
        });

        when(session.send(any())).thenReturn(Mono.empty());
        when(session.receive()).thenReturn(Flux.empty());
        when(session.closeStatus()).thenReturn(Mono.empty());

        when(registry.registerSession(any(), any(), any())).thenReturn(Mono.empty());
        when(broadcastService.register(any())).thenReturn(Mono.empty());
        when(broadcastService.broadcastSystemMessage(any())).thenReturn(Mono.empty());
        when(processor.cleanupSession(any(), any())).thenReturn(Mono.empty());
        when(broadcastService.unregister(any())).thenReturn(Mono.empty());
        when(heartbeatService.heartbeat(any())).thenReturn(Flux.empty());
        when(idleTimeoutMonitor.monitor(any(), any(), any(), any())).thenReturn(Mono.empty());

        Mono<Void> result = handler.handle(session);

        StepVerifier.create(result)
                .verifyComplete();

        verify(registry).registerSession("user1", "fp1", session);
        verify(broadcastService).register(session);
        verify(broadcastService).broadcastSystemMessage("user1 entrou");
    }

    @Test
    void testHandleUnauthenticatedUserClosesSession() {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getAttributes()).thenReturn(new HashMap<>());
        when(session.close()).thenReturn(Mono.empty());

        Mono<Void> result = handler.handle(session);

        StepVerifier.create(result)
                .verifyComplete();

        verify(session).close();
    }
}
