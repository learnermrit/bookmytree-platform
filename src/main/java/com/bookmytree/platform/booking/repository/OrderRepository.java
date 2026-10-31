package com.bookmytree.platform.booking.repository;

import com.bookmytree.platform.booking.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserId(UUID userId);

    boolean existsByItemTypeAndItemId(Order.ItemType itemType, UUID itemId);

    Optional<Order> findTopByItemTypeAndItemIdOrderByCreatedAtDesc(Order.ItemType itemType, UUID itemId);
}