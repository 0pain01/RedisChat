package com.freightfox.chatapp.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.freightfox.chatapp.DTO.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    //this will create the room in redis
    public boolean createRoom(String roomId) {
        String key = "chatroom:meta:" + roomId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) return false;
        redisTemplate.opsForHash().put(key, "createdAt", System.currentTimeMillis());
        return true;
    }

    //this will add participant to chatroom when he joins
    public void addParticipant(String roomId, String participant) {
        redisTemplate.opsForSet().add("chatroom:participants:" + roomId, participant);
    }

    //this will save the message in realtime in redis when published
    public void saveMessage(String roomId, ChatMessage message) {
        try {
            String msgJson = objectMapper.writeValueAsString(message);
            redisTemplate.opsForList().rightPush("chatroom:messages:" + roomId, msgJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    //this will extract history chats as per limit
    public List<ChatMessage> getLastMessages(String roomId, int limit) {
        List<Object> list = redisTemplate.opsForList().range("chatroom:messages:" + roomId, -limit, -1);
        assert list != null;
        return list.stream().map(obj -> {
            try {
                return objectMapper.readValue(obj.toString(), ChatMessage.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    //this will check if the chat room with same name exists or not
    public boolean roomExists(String roomId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("chatroom:meta:" + roomId));
    }

    //deleting the key for specific room
    public void deleteRoom(String roomId) {
        redisTemplate.delete("chatroom:meta:" + roomId);
        redisTemplate.delete("chatroom:participants:" + roomId);
        redisTemplate.delete("chatroom:messages:" + roomId);
    }
}
