package com.bookmytree.platform.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class FarmerVerificationRequestDto {
    @NotNull(message = "Farmer user ID is required")
    private UUID farmerId;

    @NotBlank(message = "Government ID info is required")
    private String governmentIdInfo;

    @NotBlank(message = "Bank details are required")
    private String bankDetailsInfo;
}