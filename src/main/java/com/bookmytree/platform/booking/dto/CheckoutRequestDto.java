package com.bookmytree.platform.booking.dto;

import com.bookmytree.platform.booking.entity.Order;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CheckoutRequestDto {
    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Branch ID is required")
    private UUID branchId;

    private Order.YieldPreference yieldPreference;
}