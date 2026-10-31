package com.bookmytree.platform.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTreeBranchRequestDto {
    @NotBlank(message = "Branch identifier is required")
    private String branchIdentifier;

    @NotNull(message = "Estimated yield is required")
    private Double estimatedYieldKg;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    private boolean available = true;
}
