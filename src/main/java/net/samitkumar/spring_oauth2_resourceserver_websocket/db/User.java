package net.samitkumar.spring_oauth2_resourceserver_websocket.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
public record User(@Id Long id, String username) {
}
