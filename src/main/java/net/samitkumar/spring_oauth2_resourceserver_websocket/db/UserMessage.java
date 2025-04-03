package net.samitkumar.spring_oauth2_resourceserver_websocket.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("messages")
public record UserMessage(@Id Long id, Long senderId, Long receiverId, String content,
                   @ReadOnlyProperty LocalDateTime createdAt, @ReadOnlyProperty Boolean isRead) {
}
