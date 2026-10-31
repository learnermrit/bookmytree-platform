package com.bookmytree.platform.adoption.dto;

import com.bookmytree.platform.adoption.entity.AdoptionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserAdoptionRecordResponseDto {
    private UUID id;
    private UUID userId;
    private UUID listingId;
    private String listingTitle;
    private String listingName;
    private Integer quantity;
    private BigDecimal amountPaid;
    private Integer pointsAwarded;
    private BigDecimal carbonOffsetKg;
    private AdoptionStatus status;
    private LocalDateTime createdAt;
}
