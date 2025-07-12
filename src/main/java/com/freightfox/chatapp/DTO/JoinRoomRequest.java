package com.freightfox.chatapp.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinRoomRequest {
    @NotBlank(message = "Participant is required")
    private String participant;
}
