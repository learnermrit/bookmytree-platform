package com.bookmytree.platform.adoption.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AdoptionCheckoutRequestDto {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Listing ID is required")
    private UUID listingId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
