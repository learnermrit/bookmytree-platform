package com.bookmytree.platform.search.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class FieldResponseDto {
    private UUID id;
    private String name;
    private UUID farmerId;
    private Double latitude;
    private Double longitude;
    private Double rating;
    private Integer totalTreesCount;
}