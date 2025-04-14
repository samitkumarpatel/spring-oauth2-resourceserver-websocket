package net.samitkumar.spring_oauth2_resourceserver_websocket.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.samitkumar.spring_oauth2_resourceserver_websocket.db.UserMessage;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.Objects;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebSocketEvents {
    final SimpMessagingTemplate simpMessagingTemplate;

    @EventListener
    public void onSessionConnect(SessionConnectEvent event) {
        var user = Objects.requireNonNull(event.getUser()).getName();
        simpMessagingTemplate.convertAndSend(
                "/topic/public",
                new OutboundMessage(
                        Event.CONNECT,
                        new UserMessage(null, 0L, 0L, "User " + user + " joined", LocalDateTime.now(), false)
                )
        );
    }

    @EventListener
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        var user = Objects.requireNonNull(event.getUser()).getName();
        simpMessagingTemplate.convertAndSend(
                "/topic/public",
                new OutboundMessage(
                        Event.DISCONNECT,
                        new UserMessage(null, 0L, 0L, "User " + user + " left", LocalDateTime.now(), false)
                )
        );
    }
}
