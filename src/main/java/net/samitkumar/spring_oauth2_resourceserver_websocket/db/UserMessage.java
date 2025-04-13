package net.samitkumar.spring_oauth2_resourceserver_websocket.db;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("messages")
public record UserMessage(
        @Id Long id,
        @JsonProperty("from") Long senderId,
        @JsonProperty("to") Long receiverId,
        @JsonAlias("message") String content,
        @ReadOnlyProperty LocalDateTime createdAt,
        @ReadOnlyProperty Boolean isRead) { }
