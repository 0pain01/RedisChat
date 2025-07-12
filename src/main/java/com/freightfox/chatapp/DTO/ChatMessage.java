package com.freightfox.chatapp.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.Instant;

@Data
public class ChatMessage {
    @NotBlank(message = "Participant is required")
    private String participant;
    @NotBlank(message = "Message is required")
    private String message;
    private Instant timestamp = Instant.now();
}
