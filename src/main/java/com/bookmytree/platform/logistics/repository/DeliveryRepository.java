package com.bookmytree.platform.logistics.repository;

import com.bookmytree.platform.logistics.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    Optional<Delivery> findByOrderId(UUID orderId);
    Optional<Delivery> findByTrackingNumber(String trackingNumber);
}