package com.bookmytree.platform.inventory.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TreeResponseDto {
    private UUID id;
    private UUID fieldId;
    private UUID bookingId;
    private boolean booked;
    private String species;
    private String stage;
    private Double latitude;
    private Double longitude;
    private List<TreeBranchResponseDto> branches;
}
