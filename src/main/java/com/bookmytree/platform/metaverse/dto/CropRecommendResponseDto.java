package com.bookmytree.platform.metaverse.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CropRecommendResponseDto {
    private String soilType;
    private Double phLevel;
    private List<String> recommendedSpecies;
    private String suitabilityScore;
}