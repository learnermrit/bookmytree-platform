package com.bookmytree.platform.inventory.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class BulkUpdateTreesRequestDto {

    @NotEmpty(message = "At least one tree ID is required")
    private List<UUID> treeIds;

    private String stage;

    private BigDecimal basePrice;

    private String branchIdentifier;

    private Double estimatedYieldKg;

    private boolean addBranch = false;
}
