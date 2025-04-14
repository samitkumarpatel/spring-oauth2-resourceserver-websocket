package net.samitkumar.spring_oauth2_resourceserver_websocket;

import lombok.RequiredArgsConstructor;
import net.samitkumar.spring_oauth2_resourceserver_websocket.db.UserMessage;
import net.samitkumar.spring_oauth2_resourceserver_websocket.websocket.Event;
import net.samitkumar.spring_oauth2_resourceserver_websocket.websocket.OutboundMessage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.Objects;

@SpringBootApplication
@RequiredArgsConstructor
public class SpringOauth2ResourceserverWebsocketApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringOauth2ResourceserverWebsocketApplication.class, args);
	}

	@Bean
	CorsFilter corsFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOriginPattern("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return new CorsFilter(source);
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

