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

    public List<OnlineUser> findAll() {
        return redisTemplate.keys("*").stream()
                .map(key -> (OnlineUser) redisTemplate.opsForValue().get(key))
                .toList();
    }

    public void save(OnlineUser onlineUser) {
        redisTemplate.opsForValue().set(onlineUser.id(), onlineUser);
    }

    public Map findById(String id) {
        var onlineUser = (OnlineUser) redisTemplate.opsForValue().get(id);
        if(Objects.nonNull(onlineUser)) {
            return new ObjectMapper().convertValue(onlineUser, Map.class);
        }
        return Map.of();
    }

    public void deleteById(String id) {
        redisTemplate.delete(id);
    }
}
