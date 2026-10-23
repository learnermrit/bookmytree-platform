package com.bookmytree.platform.logistics.dto;

import com.bookmytree.platform.logistics.entity.Delivery;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DeliveryResponseDto {
    private UUID id;
    private UUID orderId;
    private String trackingNumber;
    private Delivery.DeliveryStatus status;
    private String courierAgentName;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime createdAt;
}