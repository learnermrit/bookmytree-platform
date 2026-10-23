package com.bookmytree.platform.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class DispatchRequestDto {
    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotBlank(message = "Courier agent name is required")
    private String courierAgentName;
}