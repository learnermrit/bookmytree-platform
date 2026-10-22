package com.bookmytree.platform.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiChatRequestDto {
    @NotBlank(message = "Message cannot be blank")
    private String message;
}