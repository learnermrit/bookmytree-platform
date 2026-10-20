package com.bookmytree.platform.booking.service;

import com.bookmytree.platform.booking.entity.Order;
import com.bookmytree.platform.booking.repository.OrderRepository;
import com.bookmytree.platform.common.exception.ResourceNotFoundException;
import com.bookmytree.platform.inventory.entity.TreeBranch;
import com.bookmytree.platform.inventory.repository.TreeBranchRepository;
import com.bookmytree.platform.inventory.service.BranchLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bookmytree.platform.common.exception.BadRequestException;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final OrderRepository orderRepository;
    private final TreeBranchRepository treeBranchRepository;
    private final BranchLockService branchLockService;

    @Transactional
    public Order checkoutBranch(UUID userId, UUID branchId, Order.YieldPreference yieldPreference) {
        log.info("Initiating checkout for user: {} on branch: {}", userId, branchId);

        // 1. Acquire Redlock for this specific branch (Wait up to 3 seconds, lease for 10 seconds)
        RLock lock = branchLockService.acquireBranchLock(branchId.toString(), 3, 10);

        try {
            // 2. Fetch the branch or throw exception if not found
            TreeBranch branch = treeBranchRepository.findById(branchId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tree branch not found with ID: " + branchId));

            // 3. Check availability
            // if (!branch.isAvailable()) {
            //     log.warn("Checkout failed: Branch {} is already booked/unavailable.", branchId);
            //     throw new IllegalStateException("Selected tree branch is no longer available.");
            // }

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
            log.info("Order successfully created with ID: {} for branch: {}", savedOrder.getId(), branchId);

            return savedOrder;

        } finally {
            // 6. Always release the lock
            branchLockService.releaseLock(lock, branchId.toString());
        }
    }
}