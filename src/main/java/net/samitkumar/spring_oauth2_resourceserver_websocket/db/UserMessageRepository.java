package net.samitkumar.spring_oauth2_resourceserver_websocket.db;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserMessageRepository extends ListCrudRepository<UserMessage, Long> {


    @Query("""
        SELECT * FROM messages WHERE (sender_id = :senderId AND receiver_id = :receiverId)
         OR (sender_id = :receiverId AND receiver_id = :senderId)
         ORDER BY created_at ASC
    """)
    List<UserMessage> findMessagesBetweenUsers(Long senderId, Long receiverId);

    @Query("""
        SELECT COUNT(*)
        FROM messages m
        WHERE m.receiver_id = :me AND m.sender_id = :you AND m.is_read = FALSE
    """)
    Long countUnreadMessage(@Param("me") Long me, @Param("you") Long you);

    @Modifying
    @Query("UPDATE messages SET is_read = TRUE WHERE id = :id")
    void markMessageAsRead(@Param("id") Long id);
}
