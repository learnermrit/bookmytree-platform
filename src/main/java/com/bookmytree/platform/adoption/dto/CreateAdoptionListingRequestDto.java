package com.bookmytree.platform.adoption.dto;

import com.bookmytree.platform.adoption.entity.AdoptionCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateAdoptionListingRequestDto {

    @NotNull(message = "Farmer ID is required")
    private UUID farmerId;

    @NotNull(message = "Adoption category is required")
    private AdoptionCategory category;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Plant or crop name is required")
    private String name;

    @NotNull(message = "Quantity available is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantityAvailable;

    @NotNull(message = "Listing price is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Green points reward is required")
    @Min(value = 0, message = "Points reward cannot be negative")
    private Integer pointsReward = 0;

    @NotNull(message = "Carbon offset is required")
    @DecimalMin(value = "0.00", message = "Carbon offset cannot be negative")
    private BigDecimal carbonOffsetKg = BigDecimal.ZERO;

    private String description;
    private String location;
    private String imageUrl;
    private String unitLabel = "units";
}
