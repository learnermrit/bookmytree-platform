package com.bookmytree.platform.ai.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiChatResponseDto {
    private String response;
    private String modelUsed;
}