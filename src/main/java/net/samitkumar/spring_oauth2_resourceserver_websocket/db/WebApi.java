package net.samitkumar.spring_oauth2_resourceserver_websocket.db;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

@Component
public class WebApi {
    @Bean
    RouterFunction<ServerResponse> routerFunction() {
        return RouterFunctions
                .route()
                .path("/message", builder -> builder
                        .POST("", request -> ServerResponse.noContent().build())
                        .PATCH("/{messageId}", request -> ServerResponse.noContent().build())
                        .GET("/conversation/{id}", request -> ServerResponse.ok().body("Hello World"))
                        .GET("/conversation/{id}/unread", request -> ServerResponse.ok().body("Hello World"))
                )
                .build();
    }
}
