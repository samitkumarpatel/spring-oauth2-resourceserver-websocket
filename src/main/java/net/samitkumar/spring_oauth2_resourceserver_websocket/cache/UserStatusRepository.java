package net.samitkumar.spring_oauth2_resourceserver_websocket.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserStatusRepository {
    final RedisTemplate<String, Object> redisTemplate;

    public List<UserStatus> findAll() {
        return redisTemplate.keys("*").stream()
                .map(key -> (UserStatus) redisTemplate.opsForValue().get(key))
                .toList();
    }

    public void save(UserStatus userStatus) {
        redisTemplate.opsForValue().set(userStatus.id(), userStatus);
    }

    public Map findById(String id) {
        var onlineUser = (UserStatus) redisTemplate.opsForValue().get(id);
        if(Objects.nonNull(onlineUser)) {
            return new ObjectMapper().convertValue(onlineUser, Map.class);
        }
        return Map.of();
    }

    public void deleteById(String id) {
        redisTemplate.delete(id);
    }
}
