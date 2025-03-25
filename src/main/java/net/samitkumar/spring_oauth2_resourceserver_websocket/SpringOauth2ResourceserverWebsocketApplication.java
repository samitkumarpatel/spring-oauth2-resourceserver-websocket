package net.samitkumar.spring_oauth2_resourceserver_websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.autoconfigure.security.servlet.StaticResourceRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@SpringBootApplication
@EnableWebSecurity
public class SpringOauth2ResourceserverWebsocketApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringOauth2ResourceserverWebsocketApplication.class, args);
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
						.anyRequest().authenticated()
				)
				.oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults()));
		return http.build();
	}
}

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

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
}