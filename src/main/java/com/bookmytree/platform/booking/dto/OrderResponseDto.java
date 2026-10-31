package com.bookmytree.platform.booking.dto;

import com.bookmytree.platform.booking.entity.Order;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class OrderResponseDto {
    private UUID id;
    private UUID userId;
    private UUID itemId;
    private UUID fieldId;
    private String passCode;
    private String otpCode;
    private String qrCodeUrl;
    private Order.ItemType itemType;
    private Integer percentageShare;
    private BigDecimal totalAmount;
    private Order.OrderStatus status;
    private Order.YieldPreference yieldPreference;
    private LocalDateTime createdAt;
}