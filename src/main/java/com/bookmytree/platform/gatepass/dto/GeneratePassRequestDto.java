package com.bookmytree.platform.gatepass.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class GeneratePassRequestDto {
    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Field ID is required")
    private UUID fieldId;
}