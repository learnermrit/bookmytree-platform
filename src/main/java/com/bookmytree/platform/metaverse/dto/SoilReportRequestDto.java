package com.bookmytree.platform.metaverse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class SoilReportRequestDto {
    @NotNull(message = "Field ID is required")
    private UUID fieldId;

    @NotNull(message = "pH level is required")
    private Double phLevel;

    @NotNull(message = "Nitrogen content is required")
    private Double nitrogenContentKgPerHectare;

    @NotNull(message = "Phosphorus content is required")
    private Double phosphorusContentKgPerHectare;

    @NotNull(message = "Potassium content is required")
    private Double potassiumContentKgPerHectare;

    @NotBlank(message = "Soil type is required")
    private String soilType;

    @NotNull(message = "Test date is required")
    private LocalDate testDate;

    private String terrainAssetUrl;
}