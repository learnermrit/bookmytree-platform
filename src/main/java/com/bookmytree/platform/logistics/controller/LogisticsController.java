package com.bookmytree.platform.logistics.controller;

import com.bookmytree.platform.logistics.dto.DeliveryResponseDto;
import com.bookmytree.platform.logistics.dto.DispatchRequestDto;
import com.bookmytree.platform.logistics.entity.Delivery;
import com.bookmytree.platform.logistics.service.LogisticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class LogisticsController {

    private final LogisticsService logisticsService;

    @PostMapping("/dispatch")
    public ResponseEntity<DeliveryResponseDto> dispatchOrder(@Valid @RequestBody DispatchRequestDto request) {
        DeliveryResponseDto response = logisticsService.dispatchOrder(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<DeliveryResponseDto> getDeliveryByOrder(@PathVariable UUID orderId) {
        DeliveryResponseDto response = logisticsService.getDeliveryByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{deliveryId}/status")
    public ResponseEntity<DeliveryResponseDto> updateStatus(
            @PathVariable UUID deliveryId,
            @RequestParam Delivery.DeliveryStatus status
    ) {
        DeliveryResponseDto response = logisticsService.updateDeliveryStatus(deliveryId, status);
        return ResponseEntity.ok(response);
    }
}