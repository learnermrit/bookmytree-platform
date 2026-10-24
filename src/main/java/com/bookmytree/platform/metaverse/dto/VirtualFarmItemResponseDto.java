package com.bookmytree.platform.metaverse.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class VirtualFarmItemResponseDto {
    private UUID id;
    private UUID userId;
    private String cropSpecies;
    private Double latitude;
    private Double longitude;
    private String metaversePlotId;
    private Integer growthStage;
}