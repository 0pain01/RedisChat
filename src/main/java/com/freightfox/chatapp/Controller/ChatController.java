package com.freightfox.chatapp.Controller;

import com.freightfox.chatapp.DTO.ChatMessage;
import com.freightfox.chatapp.DTO.CreateRoomRequest;
import com.freightfox.chatapp.DTO.JoinRoomRequest;
import com.freightfox.chatapp.RedisMessage.RedisMessagePublisher;
import com.freightfox.chatapp.Repository.ChatRoomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatapp/chatrooms")
public class ChatController {
    private final ChatRoomRepository repo;
    private final RedisMessagePublisher publisher;

    public ChatController(ChatRoomRepository repo, RedisMessagePublisher publisher) {
        this.repo = repo;
        this.publisher = publisher;
    }

    //endpoint to create a new chatroom
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody CreateRoomRequest request) {
        if (request.getRoomName() == null || request.getRoomName().isBlank()) {
            throw new IllegalArgumentException("Room name is required");
        }
        //will throw error if the same chatroom name exists
        if (!repo.createRoom(request.getRoomName())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Room already exists"));
        }
        return ResponseEntity.ok(Map.of(
                "message", "Chat room '" + request.getRoomName() + "' created successfully.",
                "roomId", request.getRoomName(),
                "status", "success"
        ));
    }

    //endpoint to join the specific chatroom
    @PostMapping("/{roomId}/join")
    public ResponseEntity<?> joinRoom(@PathVariable String roomId,@RequestBody JoinRoomRequest req) {
        if (req.getParticipant() == null || req.getParticipant().isBlank()) {
            throw new IllegalArgumentException("Participant is required");
        }

        if (!repo.roomExists(roomId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Room does not exist"));
        }
        repo.addParticipant(roomId, req.getParticipant());
        return ResponseEntity.ok(Map.of(
                "message", "User '" + req.getParticipant() + "' joined chat room '" + roomId + "'.",
                "status", "success"
        ));
    }

    //endpoint to send the message to particular room
    @PostMapping("/{roomId}/messages")
    public ResponseEntity<?> sendMessage(@PathVariable String roomId,@RequestBody ChatMessage message) {
        // if the participant is empty in json input it throws an error message
        if (message.getParticipant() == null || message.getParticipant().isBlank()) {
            throw new IllegalArgumentException("Participant is required");
        }
        // if the message is empty in json input it throws an error message
        if (message.getMessage() == null || message.getMessage().isBlank()) {
            throw new IllegalArgumentException("Message text is required");
        }

        //will check if the room exists
        if (!repo.roomExists(roomId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Room does not exist"));
        }
        repo.saveMessage(roomId, message);
        publisher.publish(roomId, message);
        return ResponseEntity.ok(Map.of(
                "message", "Message sent successfully.",
                "status", "success"
        ));
    }

    //endpoint to retrieve message from a chatroom with default limit 10
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<?> getMessages(@PathVariable String roomId,
                                         @RequestParam(defaultValue = "10") int limit) {
        if (!repo.roomExists(roomId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Room does not exist"));
        }
        List<ChatMessage> messages = repo.getLastMessages(roomId, limit);
        return ResponseEntity.ok(Map.of("messages", messages));
    }

    //endpoint to delete the specific room
    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable String roomId) {
        if (!repo.roomExists(roomId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Room does not exist"));
        }

        repo.deleteRoom(roomId);
        return ResponseEntity.ok(Map.of(
                "message", "Chat room '" + roomId + "' deleted successfully.",
                "status", "success"
        ));
    }

}
