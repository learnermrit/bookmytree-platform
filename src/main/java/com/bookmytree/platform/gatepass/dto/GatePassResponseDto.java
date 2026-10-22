package com.bookmytree.platform.gatepass.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class GatePassResponseDto {
    private UUID id;
    private UUID userId;
    private UUID fieldId;
    private String passCode;
    private String otpCode;
    private LocalDateTime validUntil;
    private boolean isUsed;
}