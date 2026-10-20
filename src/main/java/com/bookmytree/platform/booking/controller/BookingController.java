package com.bookmytree.platform.booking.controller;

import com.bookmytree.platform.booking.entity.Order;
import com.bookmytree.platform.booking.service.BookingService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@RequestBody CheckoutRequest request) {
        Order order = bookingService.checkoutBranch(
                request.getUserId(),
                request.getBranchId(),
                request.getYieldPreference()
        );
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @Data
    public static class CheckoutRequest {
        private UUID userId;
        private UUID branchId;
        private Order.YieldPreference yieldPreference;
    }
}