package net.samitkumar.spring_oauth2_resourceserver_websocket.db;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserMessageRepository extends ListCrudRepository<UserMessage, Long> {

    List<UserMessage> findUserMessageBySenderIdAndReceiverIdOrderByCreatedAt(Long senderId, Long receiverId);

    @Query("""
        SELECT m.id, u1.id AS senderId, u2.id AS receiverId, m.content, m.created_at, m.is_read
        FROM messages m
        JOIN users u1 ON m.sender_id = u1.id
        JOIN users u2 ON m.receiver_id = u2.id
        WHERE (m.sender_id = :userId1 AND m.receiver_id = :userId2)
           OR (m.sender_id = :userId2 AND m.receiver_id = :userId1)
        ORDER BY m.created_at
    """)
    List<UserMessage> findMessagesBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("""
        SELECT COUNT(*)
        FROM messages m
        WHERE m.sender_id = :me AND m.receiver_id = :you AND m.is_read = FALSE
    """)
    Long countUnreadMessage(@Param("me") Long me, @Param("you") Long you);

    @Modifying
    @Query("UPDATE messages SET is_read = TRUE WHERE id = :id")
    void markMessageAsRead(@Param("id") Long id);
}
