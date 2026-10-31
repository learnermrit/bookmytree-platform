package com.bookmytree.platform.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTreeRequestDto {
    @NotBlank(message = "Tree species is required")
    private String species;

    @NotBlank(message = "Tree stage is required")
    private String stage;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;
}
