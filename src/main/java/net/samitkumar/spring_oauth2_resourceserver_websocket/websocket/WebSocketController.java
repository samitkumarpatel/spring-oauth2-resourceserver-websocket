package net.samitkumar.spring_oauth2_resourceserver_websocket.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.samitkumar.spring_oauth2_resourceserver_websocket.db.UserMessage;
import net.samitkumar.spring_oauth2_resourceserver_websocket.db.UserMessageRepository;
import net.samitkumar.spring_oauth2_resourceserver_websocket.db.UserRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
class WebSocketController {
    final SimpMessagingTemplate simpMessagingTemplate;
    final JwtDecoder jwtDecoder;
    final UserMessageRepository userMessageRepository;
    final UserRepository userRepository;

    @GetMapping("/who-am-i")
    //@CrossOrigin(originPatterns = "*")
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
                new UserMessage(null, 0L, 0L, message.message(), LocalDateTime.now(), false)
        );
    }

    @MessageMapping("/chat/private")
    @SendToUser("/queue/private")
    public OutboundMessage processPrivateMessage(Principal principal, InboundMessage message) {
        log.info("####Private Message {} from {}", message, principal);

        var meId = userRepository.findUserByUsername(principal.getName()).orElseThrow().id();
        var receiverId = userRepository.findUserByUsername(message.to()).orElseThrow().id();
        var dbResponse = userMessageRepository.save(new UserMessage(null, meId, receiverId, message.message(), null, false));

        var messageToSender = new OutboundMessage(
                Event.MESSAGE_TO_USER,
                dbResponse
        );

        var messageToReceiver = new OutboundMessage(
                Event.MESSAGE_FROM_USER,
                dbResponse
        );
        simpMessagingTemplate.convertAndSendToUser(message.to(), "/queue/private", messageToReceiver);
        return messageToSender;
    }
}
