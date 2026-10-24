package com.bookmytree.platform.metaverse.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class GreenPointsResponseDto {
    private UUID userId;
    private Integer pointsBalance;
    private BigDecimal carbonOffsetKg;
}