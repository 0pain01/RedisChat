package com.freightfox.chatapp;

import com.freightfox.chatapp.DTO.ChatMessage;
import com.freightfox.chatapp.DTO.CreateRoomRequest;
import com.freightfox.chatapp.DTO.JoinRoomRequest;
import com.freightfox.chatapp.RedisMessage.RedisMessagePublisher;
import com.freightfox.chatapp.Repository.ChatRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private ChatRoomRepository chatRoomRepository;

    @SuppressWarnings("removal")
    @MockBean
    private RedisMessagePublisher redisMessagePublisher;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        when(chatRoomRepository.roomExists("general")).thenReturn(true);
    }

    //this will test when the room is successfully created
    @Test
    void createRoom_success() throws Exception {
        CreateRoomRequest req = new CreateRoomRequest();
        req.setRoomName("general");

        when(chatRoomRepository.createRoom("general")).thenReturn(true);

        mockMvc.perform(post("/api/chatapp/chatrooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value("general"));
    }

    //this will test when the room with same name is being tried again
    @Test
    void createRoom_duplicateName() throws Exception {
        CreateRoomRequest req = new CreateRoomRequest();
        req.setRoomName("general");

        when(chatRoomRepository.createRoom("general")).thenReturn(false);

        mockMvc.perform(post("/api/chatapp/chatrooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Room already exists"));
    }

    //this will test the successfully joining the room
    @Test
    void joinRoom_success() throws Exception {
        JoinRoomRequest req = new JoinRoomRequest();
        req.setParticipant("guest_user");

        mockMvc.perform(post("/api/chatapp/chatrooms/general/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User 'guest_user' joined chat room 'general'."));
    }

    //this will test when the user entered the room which is not present
    @Test
    void joinRoom_roomNotFound() throws Exception {
        when(chatRoomRepository.roomExists("invalid")).thenReturn(false);

        JoinRoomRequest req = new JoinRoomRequest();
        req.setParticipant("user");

        mockMvc.perform(post("/api/chatapp/chatrooms/invalid/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Room does not exist"));
    }

    //this will test the successful message sent by user
    @Test
    void sendMessage_success() throws Exception {
        ChatMessage msg = new ChatMessage();
        msg.setParticipant("guest_user");
        msg.setMessage("Hello");
        msg.setTimestamp(Instant.now());

        mockMvc.perform(post("/api/chatapp/chatrooms/general/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Message sent successfully."));
    }

    //this will test when the message sent to a nonexistent room
    @Test
    void sendMessage_roomNotFound() throws Exception {
        when(chatRoomRepository.roomExists("invalid")).thenReturn(false);

        ChatMessage msg = new ChatMessage();
        msg.setParticipant("guest_user");
        msg.setMessage("Hello");

        mockMvc.perform(post("/api/chatapp/chatrooms/invalid/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msg)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Room does not exist"));
    }

    //this will test when the retrieving the old message successfully
    @Test
    void getMessages_success() throws Exception {
        ChatMessage msg = new ChatMessage();
        msg.setParticipant("guest_user");
        msg.setMessage("Hi");
        msg.setTimestamp(Instant.now());

        when(chatRoomRepository.getLastMessages("general", 1)).thenReturn(List.of(msg));

        mockMvc.perform(get("/api/chatapp/chatrooms/general/messages?limit=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages[0].message").value("Hi"));
    }

    //this will test if retrieving message rom a nonexistent room
    @Test
    void getMessages_roomNotFound() throws Exception {
        when(chatRoomRepository.roomExists("invalid")).thenReturn(false);

        mockMvc.perform(get("/api/chatapp/chatrooms/invalid/messages?limit=10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Room does not exist"));
    }

    //this will test the empty user trying to join a room
    @Test
    void joinRoom_missingParticipant() throws Exception {
        String payload = "{}"; // missing participant

        mockMvc.perform(post("/api/chatapp/chatrooms/general/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    //this will test when the message field is empty when trying to send one
    @Test
    void sendMessage_missingMessageField() throws Exception {
        String payload = """
        {
          "participant": "guest_user"
        }
        """;

        mockMvc.perform(post("/api/chatapp/chatrooms/general/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    //this will test when the participant name/id is not provided
    @Test
    void sendMessage_missingParticipantField() throws Exception {
        String payload = """
        {
          "message": "Hello!"
        }
        """;

        mockMvc.perform(post("/api/chatapp/chatrooms/general/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }


    //this will test when the room with empty room is trying to be created
    @Test
    void createRoom_emptyRoomName() throws Exception {
        String payload = """
        {
          "roomName": ""
        }
        """;

        mockMvc.perform(post("/api/chatapp/chatrooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }


    //this will test when room deletion is successful
    @Test
    void deleteRoom_success() throws Exception {
        when(chatRoomRepository.roomExists("general")).thenReturn(true);

        mockMvc.perform(delete("/api/chatapp/chatrooms/general"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Chat room 'general' deleted successfully."));
    }
    
    //this will test when the non-existent room is being deleted
    @Test
    void deleteRoom_notFound() throws Exception {
        when(chatRoomRepository.roomExists("nonexistent")).thenReturn(false);

        mockMvc.perform(delete("/api/chatapp/chatrooms/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Room does not exist"));
    }
}
