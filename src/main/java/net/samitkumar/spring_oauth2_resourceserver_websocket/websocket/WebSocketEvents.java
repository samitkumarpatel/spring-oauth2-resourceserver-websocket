package net.samitkumar.spring_oauth2_resourceserver_websocket.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.samitkumar.spring_oauth2_resourceserver_websocket.cache.UserStatus;
import net.samitkumar.spring_oauth2_resourceserver_websocket.cache.UserStatusRepository;
import net.samitkumar.spring_oauth2_resourceserver_websocket.db.UserMessage;
import net.samitkumar.spring_oauth2_resourceserver_websocket.db.UserRepository;
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
    final UserStatusRepository userStatusRepository;
    final UserRepository userRepository;

    @EventListener
    public void onSessionConnect(SessionConnectEvent event) {
        var user = Objects.requireNonNull(event.getUser()).getName();
        var dbUser = userRepository.findUserByUsername(user).orElseThrow();

        //mark the user online
        userStatusRepository.save(new UserStatus(
                String.valueOf(dbUser.id()),
                dbUser.username(),
                true)
        );
        //send a websocket message to all users
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
        var dbUser = userRepository.findUserByUsername(user).orElseThrow();

        //mark the user offline
        userStatusRepository.save(new UserStatus(
                String.valueOf(dbUser.id()),
                dbUser.username(),
                false)
        );

        //send a websocket message to all users
        simpMessagingTemplate.convertAndSend(
                "/topic/public",
                new OutboundMessage(
                        Event.DISCONNECT,
                        new UserMessage(null, 0L, 0L, "User " + user + " left", LocalDateTime.now(), false)
                )
        );
    }
}
