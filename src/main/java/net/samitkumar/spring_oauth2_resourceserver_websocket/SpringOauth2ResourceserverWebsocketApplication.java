package net.samitkumar.spring_oauth2_resourceserver_websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;

@SpringBootApplication
@RequiredArgsConstructor
public class SpringOauth2ResourceserverWebsocketApplication {

	final SimpMessagingTemplate simpMessagingTemplate;

	public static void main(String[] args) {
		SpringApplication.run(SpringOauth2ResourceserverWebsocketApplication.class, args);
	}

	@EventListener
	public void onSessionConnect(SessionConnectEvent event) {
		var user = Objects.requireNonNull(event.getUser()).getName();
		simpMessagingTemplate.convertAndSend("/topic/public", new OutboundMessage(Event.CONNECT, new Payload("User " + user + " joined", "ALL", "SYSTEM")));
	}

	@EventListener
	public void onSessionDisconnect(SessionDisconnectEvent event) {
		var user = Objects.requireNonNull(event.getUser()).getName();
		simpMessagingTemplate.convertAndSend("/topic/public", new OutboundMessage(Event.DISCONNECT, new Payload("User " + user + " left", "ALL", "SYSTEM")));
	}
}

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {
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
		registry.enableSimpleBroker("/queue/", "/topic/");
		// STOMP messages whose destination header begins with /app are routed to @MessageMapping methods in @Controller classes
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
					log.info("###Authorization header received: {}", authHeader);
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

@ResponseStatus(HttpStatus.FORBIDDEN)
class ForbiddenException extends RuntimeException {
	public ForbiddenException(String message) {
		super(message);
	}
}


@Configuration
@EnableWebSecurity
class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/stomp-endpoint/**").permitAll()
						.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
						.anyRequest().authenticated()
				)
				.oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()));
		return http
				.cors(Customizer.withDefaults())
				.build();
	}
}

enum Event {
	CONNECT,
	DISCONNECT,
	MESSAGE_TO_ALL,
	MESSAGE_FROM_USER,
	MESSAGE_FROM_GROUP,
	MESSAGE_TO_USER,
	MESSAGE_TO_GROUP
}
record InboundMessage(String message, String from, String to) {}
record OutboundMessage(Event event, Payload payload) {}
record Payload(String message, String from, String to) {}

@Controller
@Slf4j
@RequiredArgsConstructor
class WebSocketController {
	final SimpMessagingTemplate simpMessagingTemplate;
	final JwtDecoder jwtDecoder;

	@GetMapping("/who-am-i")
	@CrossOrigin(originPatterns = "*")
	@ResponseBody
	Map<String, Object> whoAmI(Principal principal) {
		var jwt = (Jwt) ((JwtAuthenticationToken) principal).getToken();
		return jwt.getClaims();
	}

	@MessageMapping("/chat")
	@SendTo("/topic/public")
	public OutboundMessage processMessage(Principal principal, InboundMessage message) {
		log.info("####Public Message {} from {}", message, principal);
		return new OutboundMessage(
				Event.MESSAGE_TO_ALL,
				new Payload(message.message(), principal.getName(), "ALL")
		);
	}

	@MessageMapping("/chat/private")
	@SendToUser("/queue/private")
	public OutboundMessage processPrivateMessage(Principal principal, InboundMessage message) {
		log.info("####Private Message {} from {}", message, principal);
		var messageToSender = new OutboundMessage(
				Event.MESSAGE_TO_USER,
				new Payload(message.message(), principal.getName(), principal.getName())
		);
		var messageToReceiver = new OutboundMessage(
				Event.MESSAGE_FROM_USER,
				new Payload(message.message(), principal.getName(), message.to())
		);
		simpMessagingTemplate.convertAndSendToUser(message.to(), "/queue/private", messageToReceiver);
		return messageToSender;
	}
}

