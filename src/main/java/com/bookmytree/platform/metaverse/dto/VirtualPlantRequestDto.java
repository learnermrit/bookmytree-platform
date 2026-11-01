package com.bookmytree.platform.metaverse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class VirtualPlantRequestDto {
    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Crop species is required")
    private String cropSpecies;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private Double xPercent = 50.0;

    private Double yPercent = 50.0;

    @NotBlank(message = "Metaverse plot ID is required")
    private String metaversePlotId;
}