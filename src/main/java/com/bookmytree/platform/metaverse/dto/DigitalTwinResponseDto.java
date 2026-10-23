package com.bookmytree.platform.metaverse.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class DigitalTwinResponseDto {
    private UUID id;
    private UUID fieldId;
    private Double phLevel;
    private Double nitrogenContentKgPerHectare;
    private Double phosphorusContentKgPerHectare;
    private Double potassiumContentKgPerHectare;
    private String soilType;
    private LocalDate testDate;
    private String terrainAssetUrl;
    private String aiSuitabilityRecommendation;
}