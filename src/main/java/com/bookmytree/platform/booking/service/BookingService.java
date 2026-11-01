package com.bookmytree.platform.booking.service;

import com.bookmytree.platform.booking.dto.CheckoutRequestDto;
import com.bookmytree.platform.booking.dto.OrderResponseDto;
import com.bookmytree.platform.booking.entity.Order;
import com.bookmytree.platform.booking.repository.OrderRepository;
import com.bookmytree.platform.common.exception.BadRequestException;
import com.bookmytree.platform.common.exception.ResourceNotFoundException;
import com.bookmytree.platform.gatepass.service.GatePassService;
import com.bookmytree.platform.inventory.entity.Tree;
import com.bookmytree.platform.inventory.entity.TreeBranch;
import com.bookmytree.platform.inventory.repository.TreeBranchRepository;
import com.bookmytree.platform.inventory.repository.TreeRepository;
import com.bookmytree.platform.inventory.service.BranchLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final OrderRepository orderRepository;
    private final TreeBranchRepository treeBranchRepository;
    private final TreeRepository treeRepository;
    private final BranchLockService branchLockService;
    private final GatePassService gatePassService;

    @Transactional
    public OrderResponseDto checkoutOrder(CheckoutRequestDto request) {
        if (request == null) {
            throw new BadRequestException("Checkout request is required.");
        }

        if (request.getUserId() == null) {
            throw new BadRequestException("User ID is required.");
        }

        UUID itemId = request.getItemId() != null ? request.getItemId() : request.getBranchId();
        if (itemId == null) {
            throw new BadRequestException("Item ID is required.");
        }

        Order.ItemType itemType = request.getItemType() != null ? request.getItemType() : Order.ItemType.BRANCH;
        Order.YieldPreference yieldPreference = request.getYieldPreference() != null
                ? request.getYieldPreference()
                : Order.YieldPreference.PARCEL_DELIVERY;

        if (itemType == Order.ItemType.BRANCH) {
            return checkoutBranch(request.getUserId(), itemId, yieldPreference);
        }

        if (itemType == Order.ItemType.WHOLE_TREE) {
            return checkoutTree(request.getUserId(), itemId, yieldPreference, null);
        }

        if (itemType == Order.ItemType.PERCENTAGE_SHARE) {
            Integer percentageShare = request.getPercentageShare() != null
                    ? request.getPercentageShare()
                    : request.getQuantityOrPercentage() != null ? request.getQuantityOrPercentage() : 25;
            if (percentageShare <= 0 || percentageShare > 100) {
                throw new BadRequestException("Percentage share must be between 1 and 100.");
            }
            return checkoutTree(request.getUserId(), itemId, yieldPreference, percentageShare);
        }

        throw new BadRequestException("Unsupported item type: " + itemType);
    }

    @Transactional
    public OrderResponseDto checkoutBranch(UUID userId, UUID branchId, Order.YieldPreference yieldPreference) {
        log.info("Initiating checkout for user: {} on branch: {}", userId, branchId);

        RLock lock = branchLockService.acquireBranchLock(branchId.toString(), 3, 10);

        try {
            TreeBranch branch = treeBranchRepository.findById(branchId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tree branch not found with ID: " + branchId));

            if (!branch.isAvailable()) {
                log.warn("Checkout failed: Branch {} is already booked/unavailable.", branchId);
                throw new BadRequestException("Selected tree branch is no longer available.");
            }

            branch.setAvailable(false);
            treeBranchRepository.save(branch);

            Order order = new Order();
            order.setUserId(userId);
            order.setItemId(branchId);
            order.setItemType(Order.ItemType.BRANCH);
            order.setPercentageShare(null);
            order.setTotalAmount(branch.getPrice());
            order.setStatus(Order.OrderStatus.PAYMENT_PENDING);
            order.setYieldPreference(yieldPreference != null ? yieldPreference : Order.YieldPreference.PARCEL_DELIVERY);

            Order savedOrder = orderRepository.save(order);
            log.info("Order successfully created with ID: {}", savedOrder.getId());

            UUID fieldId = branch.getTree() != null && branch.getTree().getField() != null
                    ? branch.getTree().getField().getId()
                    : null;
            if (fieldId != null) {
                savedOrder.setFieldId(fieldId);
                orderRepository.save(savedOrder);
            }

            return mapOrder(savedOrder);
        } finally {
            branchLockService.releaseLock(lock, branchId.toString());
        }
    }

    @Transactional
    public OrderResponseDto checkoutTree(UUID userId, UUID treeId, Order.YieldPreference yieldPreference,
            Integer percentageShare) {
        log.info("Initiating checkout for user: {} on tree: {} with percentage share {}",
                userId, treeId, percentageShare == null ? "full" : percentageShare);

        RLock lock = branchLockService.acquireBranchLock(treeId.toString(), 3, 10);

        try {
            Tree tree = treeRepository.findById(treeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tree not found with ID: " + treeId));

            List<TreeBranch> branches = treeBranchRepository.findByTreeId(treeId);
            if (branches.isEmpty()) {
                throw new ResourceNotFoundException("No branches found for tree ID: " + treeId);
            }

            if (branches.stream().anyMatch(branch -> !branch.isAvailable())) {
                throw new BadRequestException("Selected tree is no longer available for checkout.");
            }

            BigDecimal totalAmount = branches.stream()
                    .map(TreeBranch::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (percentageShare != null && percentageShare > 0) {
                totalAmount = totalAmount.multiply(BigDecimal.valueOf(percentageShare))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }

            for (TreeBranch branch : branches) {
                branch.setAvailable(false);
            }
            treeBranchRepository.saveAll(branches);

            Order order = new Order();
            order.setUserId(userId);
            order.setItemId(treeId);
            order.setItemType(percentageShare != null ? Order.ItemType.PERCENTAGE_SHARE : Order.ItemType.WHOLE_TREE);
            order.setPercentageShare(percentageShare);
            order.setTotalAmount(totalAmount);
            order.setStatus(Order.OrderStatus.PAYMENT_PENDING);
            order.setYieldPreference(yieldPreference != null ? yieldPreference : Order.YieldPreference.PARCEL_DELIVERY);

            Order savedOrder = orderRepository.save(order);
            log.info("Tree order successfully created with ID: {}", savedOrder.getId());

            UUID fieldId = tree.getField() != null ? tree.getField().getId() : null;
            if (fieldId != null) {
                savedOrder.setFieldId(fieldId);
                orderRepository.save(savedOrder);
            }

            return mapOrder(savedOrder);
        } finally {
            branchLockService.releaseLock(lock, treeId.toString());
        }
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByUser(UUID userId) {
        log.info("Fetching orders for user ID: {}", userId);
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapOrder)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(UUID orderId) {
        log.info("Fetching order details for ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        return mapOrder(order);
    }

    private OrderResponseDto mapOrder(Order order) {
        UUID fieldId = order.getFieldId();
        if (fieldId == null) {
            if (order.getItemType() == Order.ItemType.BRANCH) {
                TreeBranch branch = treeBranchRepository.findById(order.getItemId()).orElse(null);
                if (branch != null && branch.getTree() != null && branch.getTree().getField() != null) {
                    fieldId = branch.getTree().getField().getId();
                }
            } else if (order.getItemType() == Order.ItemType.WHOLE_TREE
                    || order.getItemType() == Order.ItemType.PERCENTAGE_SHARE) {
                Tree tree = treeRepository.findById(order.getItemId()).orElse(null);
                if (tree != null && tree.getField() != null) {
                    fieldId = tree.getField().getId();
                }
            }
        }

        var gatePass = fieldId != null ? gatePassService.generateGatePass(order.getUserId(), fieldId) : null;

        return OrderResponseDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .itemId(order.getItemId())
                .fieldId(fieldId)
                .itemType(order.getItemType())
                .passCode(gatePass != null ? gatePass.getPassCode() : null)
                .otpCode(gatePass != null ? gatePass.getOtpCode() : null)
                .qrCodeUrl(gatePass != null
                        ? "https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=" + gatePass.getPassCode()
                        : null)
                .percentageShare(order.getPercentageShare())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .yieldPreference(order.getYieldPreference())
                .createdAt(order.getCreatedAt())
                .build();
    }
}