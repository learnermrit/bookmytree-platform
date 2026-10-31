package com.bookmytree.platform.booking.service;

import com.bookmytree.platform.booking.dto.CheckoutRequestDto;
import com.bookmytree.platform.booking.dto.OrderResponseDto;
import com.bookmytree.platform.booking.entity.Order;
import com.bookmytree.platform.booking.repository.OrderRepository;
import com.bookmytree.platform.inventory.entity.Tree;
import com.bookmytree.platform.inventory.entity.TreeBranch;
import com.bookmytree.platform.inventory.repository.TreeBranchRepository;
import com.bookmytree.platform.inventory.repository.TreeRepository;
import com.bookmytree.platform.inventory.service.BranchLockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TreeBranchRepository treeBranchRepository;

    @Mock
    private TreeRepository treeRepository;

    @Mock
    private BranchLockService branchLockService;

    @Mock
    private RLock rLock;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void shouldCreatePercentageShareOrderWithRequestedShare() {
        UUID userId = UUID.randomUUID();
        UUID treeId = UUID.randomUUID();

        Tree tree = new Tree();
        tree.setId(treeId);

        TreeBranch branchA = new TreeBranch();
        branchA.setId(UUID.randomUUID());
        branchA.setTree(tree);
        branchA.setAvailable(true);
        branchA.setPrice(new BigDecimal("200"));

        TreeBranch branchB = new TreeBranch();
        branchB.setId(UUID.randomUUID());
        branchB.setTree(tree);
        branchB.setAvailable(true);
        branchB.setPrice(new BigDecimal("100"));

        when(branchLockService.acquireBranchLock(anyString(), anyLong(), anyLong())).thenReturn(rLock);
        when(treeRepository.findById(treeId)).thenReturn(java.util.Optional.of(tree));
        when(treeBranchRepository.findByTreeId(treeId)).thenReturn(List.of(branchA, branchB));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(UUID.randomUUID());
            return order;
        });

        CheckoutRequestDto request = new CheckoutRequestDto();
        request.setUserId(userId);
        request.setItemId(treeId);
        request.setItemType(Order.ItemType.PERCENTAGE_SHARE);
        request.setPercentageShare(25);
        request.setYieldPreference(Order.YieldPreference.RESELL_PROFIT_SHARE);

        OrderResponseDto response = bookingService.checkoutOrder(request);

        assertEquals(Order.ItemType.PERCENTAGE_SHARE, response.getItemType());
        assertEquals(25, response.getPercentageShare());
        assertEquals(new BigDecimal("75.00"), response.getTotalAmount());
    }
}
