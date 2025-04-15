package net.samitkumar.spring_oauth2_resourceserver_websocket.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.samitkumar.spring_oauth2_resourceserver_websocket.db.UserMessage;

public record OutboundMessage(Event event, @JsonProperty("payload") UserMessage payload) {
}
