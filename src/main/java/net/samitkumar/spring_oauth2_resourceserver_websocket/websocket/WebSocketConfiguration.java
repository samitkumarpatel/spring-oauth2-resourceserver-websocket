package net.samitkumar.spring_oauth2_resourceserver_websocket.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.rabbitmq.relay.host}")
    private String relayHost;

    @Value("${spring.rabbitmq.relay.port}")
    private int relayPort;

    private final JwtDecoder jwtDecoder;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/stomp-endpoint")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setHeartbeatTime(60_000);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        //registry.enableSimpleBroker("/queue/", "/topic/");
        // STOMP messages whose destination header begins with /app are routed to @MessageMapping methods in @Controller classes
        registry.enableStompBrokerRelay("/queue/", "/topic/")
                .setRelayHost(relayHost)
                .setRelayPort(relayPort)
                .setClientLogin("guest")
                .setClientPasscode("guest");

        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
                    log.debug("###Authorization header received: {}", authHeader);
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        try {
                            Jwt jwt = jwtDecoder.decode(token);
                            Authentication auth = new JwtAuthenticationToken(jwt);
                            accessor.setUser(auth);  // Set authentication in WebSocket session
                        } catch (JwtException e) {

                            // Send STOMP ERROR frame to client
                            StompHeaderAccessor errorAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
                            errorAccessor.setMessage("JWT Token is expired or invalid");
                            errorAccessor.setLeaveMutable(true);

                            return MessageBuilder.createMessage(errorAccessor.getMessage(), errorAccessor.getMessageHeaders());
                            //throw new ForbiddenException("Invalid JWT Token");
                        }
                    } else {
                        // Send STOMP ERROR frame for missing token
                        StompHeaderAccessor errorAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
                        errorAccessor.setMessage("Missing Authorization Header");
                        errorAccessor.setLeaveMutable(true);

                        return MessageBuilder.createMessage(errorAccessor.getMessage(), errorAccessor.getMessageHeaders());
                        //throw new ForbiddenException("Missing Authorization Header");
                    }
                }
                return message;
            }
        });
    }
}
