package com.freightfox.chatapp.RedisMessage;

import com.freightfox.chatapp.DTO.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisMessagePublisher {
    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(String roomId, ChatMessage message) {
        redisTemplate.convertAndSend("chatroom:" + roomId, message);
    }
}
