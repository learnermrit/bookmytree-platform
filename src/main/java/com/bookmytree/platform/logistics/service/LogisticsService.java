package com.bookmytree.platform.logistics.service;

import com.bookmytree.platform.common.exception.ResourceNotFoundException;
import com.bookmytree.platform.logistics.dto.DeliveryResponseDto;
import com.bookmytree.platform.logistics.dto.DispatchRequestDto;
import com.bookmytree.platform.logistics.entity.Delivery;
import com.bookmytree.platform.logistics.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsService {

    private final DeliveryRepository deliveryRepository;

    @Transactional
    public DeliveryResponseDto dispatchOrder(DispatchRequestDto request) {
        log.info("Dispatching harvest parcel for order ID: {}", request.getOrderId());

        String trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Delivery delivery = deliveryRepository.findByOrderId(request.getOrderId())
                .orElse(new Delivery());

        delivery.setOrderId(request.getOrderId());
        delivery.setTrackingNumber(trackingNumber);
        delivery.setStatus(Delivery.DeliveryStatus.DISPATCHED);
        delivery.setCourierAgentName(request.getCourierAgentName());
        delivery.setEstimatedDeliveryDate(LocalDateTime.now().plusDays(3)); // Default 3-day delivery window

        Delivery savedDelivery = deliveryRepository.save(delivery);
        log.info("Delivery dispatched with tracking number: {}", trackingNumber);

        return mapToDto(savedDelivery);
    }

    @Transactional(readOnly = true)
    public DeliveryResponseDto getDeliveryByOrderId(UUID orderId) {
        log.info("Fetching delivery details for order ID: {}", orderId);
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery record not found for order ID: " + orderId));
        return mapToDto(delivery);
    }

    @Transactional
    public DeliveryResponseDto updateDeliveryStatus(UUID deliveryId, Delivery.DeliveryStatus newStatus) {
        log.info("Updating delivery ID: {} to status: {}", deliveryId, newStatus);
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with ID: " + deliveryId));

        delivery.setStatus(newStatus);
        Delivery updatedDelivery = deliveryRepository.save(delivery);
        return mapToDto(updatedDelivery);
    }

    private DeliveryResponseDto mapToDto(Delivery delivery) {
        return DeliveryResponseDto.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .trackingNumber(delivery.getTrackingNumber())
                .status(delivery.getStatus())
                .courierAgentName(delivery.getCourierAgentName())
                .estimatedDeliveryDate(delivery.getEstimatedDeliveryDate())
                .createdAt(delivery.getCreatedAt())
                .build();
    }
}