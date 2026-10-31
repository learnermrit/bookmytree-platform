package com.bookmytree.platform.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BulkCreateTreesRequestDto {

    @NotBlank(message = "Tree species is required")
    private String species;

    @NotBlank(message = "Tree stage is required")
    private String stage;

    @NotNull(message = "Tree quantity is required")
    @Min(value = 1, message = "At least one tree must be created")
    private Integer count;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private String branchIdentifier = "North-Canopy-Branch";

    private Double initialEstimatedYieldKg = 18.0;

    private BigDecimal basePrice = new BigDecimal("420.00");

    private boolean addDefaultBranch = true;
}
