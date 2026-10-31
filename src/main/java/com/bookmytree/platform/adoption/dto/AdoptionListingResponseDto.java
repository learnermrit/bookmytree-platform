package com.bookmytree.platform.adoption.dto;

import com.bookmytree.platform.adoption.entity.AdoptionCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AdoptionListingResponseDto {
    private UUID id;
    private UUID farmerId;
    private AdoptionCategory category;
    private String title;
    private String name;
    private Integer quantityAvailable;
    private BigDecimal price;
    private Integer pointsReward;
    private BigDecimal carbonOffsetKg;
    private String description;
    private String location;
    private String imageUrl;
    private String unitLabel;
    private boolean active;
    private LocalDateTime createdAt;
}
