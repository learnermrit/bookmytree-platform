package com.bookmytree.platform.inventory.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class TreeBranchResponseDto {
    private UUID id;
    private UUID bookingId;
    private boolean booked;
    private String branchIdentifier;
    private Double estimatedYieldKg;
    private BigDecimal price;
    private boolean available;
}
