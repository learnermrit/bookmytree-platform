package com.bookmytree.platform.booking.service;

import com.bookmytree.platform.booking.dto.OrderResponseDto;
import com.bookmytree.platform.booking.entity.Order;
import com.bookmytree.platform.booking.repository.OrderRepository;
import com.bookmytree.platform.common.exception.BadRequestException;
import com.bookmytree.platform.common.exception.ResourceNotFoundException;
import com.bookmytree.platform.inventory.entity.TreeBranch;
import com.bookmytree.platform.inventory.repository.TreeBranchRepository;
import com.bookmytree.platform.inventory.service.BranchLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final OrderRepository orderRepository;
    private final TreeBranchRepository treeBranchRepository;
    private final BranchLockService branchLockService;

    @Transactional
    public OrderResponseDto checkoutBranch(UUID userId, UUID branchId, Order.YieldPreference yieldPreference) {
        log.info("Initiating checkout for user: {} on branch: {}", userId, branchId);
 
        // 1. Acquire Redlock for this specific branch (Wait up to 3 seconds, lease for 10 seconds)

        RLock lock = branchLockService.acquireBranchLock(branchId.toString(), 3, 10);

        try {
            // 2. Fetch the branch or throw exception if not found

            TreeBranch branch = treeBranchRepository.findById(branchId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tree branch not found with ID: " + branchId));
             // 3. Check availability

            if (!branch.isAvailable()) {
                log.warn("Checkout failed: Branch {} is already booked/unavailable.", branchId);
                throw new BadRequestException("Selected tree branch is no longer available.");
            }
            // 4. Update inventory state
            branch.setAvailable(false);
            treeBranchRepository.save(branch);
            // 5. Create and persist the order

            Order order = new Order();
            order.setUserId(userId);
            order.setItemId(branchId);
            order.setItemType(Order.ItemType.BRANCH);
            order.setTotalAmount(branch.getPrice());
            order.setStatus(Order.OrderStatus.PAYMENT_PENDING);
            order.setYieldPreference(yieldPreference != null ? yieldPreference : Order.YieldPreference.PARCEL_DELIVERY);

            Order savedOrder = orderRepository.save(order);
            log.info("Order successfully created with ID: {}", savedOrder.getId());

            return OrderResponseDto.builder()
                    .id(savedOrder.getId())
                    .userId(savedOrder.getUserId())
                    .itemId(savedOrder.getItemId())
                    .itemType(savedOrder.getItemType())
                    .totalAmount(savedOrder.getTotalAmount())
                    .status(savedOrder.getStatus())
                    .yieldPreference(savedOrder.getYieldPreference())
                    .createdAt(savedOrder.getCreatedAt())
                    .build();

        } finally {
             // 6. Always release the lock
            branchLockService.releaseLock(lock, branchId.toString());
        }
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByUser(UUID userId) {
        log.info("Fetching orders for user ID: {}", userId);
        return orderRepository.findByUserId(userId).stream()
                .map(order -> OrderResponseDto.builder()
                        .id(order.getId())
                        .userId(order.getUserId())
                        .itemId(order.getItemId())
                        .itemType(order.getItemType())
                        .totalAmount(order.getTotalAmount())
                        .status(order.getStatus())
                        .yieldPreference(order.getYieldPreference())
                        .createdAt(order.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(UUID orderId) {
        log.info("Fetching order details for ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        return OrderResponseDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .itemId(order.getItemId())
                .itemType(order.getItemType())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .yieldPreference(order.getYieldPreference())
                .createdAt(order.getCreatedAt())
                .build();
    }

}