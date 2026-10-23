package com.bookmytree.platform.ecommerce.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class StoreCheckoutRequestDto {
    @NotNull(message = "User ID is required")
    private UUID userId;

    private boolean useGreenPointsDiscount = false;
}