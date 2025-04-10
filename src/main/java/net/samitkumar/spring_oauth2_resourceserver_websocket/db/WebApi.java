package net.samitkumar.spring_oauth2_resourceserver_websocket.db;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebApi {
    final UserMessageRepository userMessageRepository;
    final UserRepository userRepository;

    @Bean
    RouterFunction<ServerResponse> routerFunction() {
        return RouterFunctions
                .route()
                .path("/message", builder -> builder
                        .POST("", this::saveUserMessage)
                        .PATCH("/{messageId}", this::updateUserMessage)
                        .GET("/conversation/{uId}", this::getUserConversation)
                        .GET("/conversation/{uId}/unread", this::getUnreadMessageCount)
                )
                .build();
    }

    private ServerResponse getUnreadMessageCount(ServerRequest request) {
        var me = request.principal().orElseThrow().getName();
        var meId = userRepository.findUserByUsername(me).orElseThrow().id();
        var uId = Long.parseLong(request.pathVariable("uId"));
        return ServerResponse.ok()
                .body(
                        Map.of("count", userMessageRepository.countUnreadMessage(meId, uId))
                );
    }

    private ServerResponse getUserConversation(ServerRequest request) {
        var me = request.principal().orElseThrow().getName();
        var meId = userRepository.findUserByUsername(me).orElseThrow().id();
        var uId = Long.parseLong(request.pathVariable("uId"));
        log.info("Getting conversation between meId: {} and uId: {}", meId, uId);
        var messages = userMessageRepository.findMessagesBetweenUsers(meId, uId);
        System.out.println("##########"+messages);
        return ServerResponse.ok().body(messages);
    }

    @SneakyThrows
    private ServerResponse updateUserMessage(ServerRequest request) {
        var requestUserMessageRequest = request.body(UserMessage.class);
        userMessageRepository.findById(requestUserMessageRequest.id())
                .ifPresentOrElse(userMessage -> {
                    userMessageRepository.markMessageAsRead(userMessage.id());
                }, () -> {
                    throw new RuntimeException("User message not found");
                });
        return ServerResponse.ok().build();
    }

    @SneakyThrows
    private ServerResponse saveUserMessage(ServerRequest request) {
        System.out.println("##########"+request.principal().get().getName());
        var me = request.principal().orElseThrow().getName();
        var user = userRepository.findUserByUsername(me).orElseThrow(() -> new RuntimeException("User not found"));
        var userMessageRequest = request.body(UserMessage.class);
        var userMessage = new UserMessage(null, user.id(), userMessageRequest.receiverId(), userMessageRequest.content(), null, null);
        return ServerResponse
                .ok()
                .body(userMessageRepository.save(userMessage));
    }
}
