package com.bookmytree.platform.booking.controller;

import com.bookmytree.platform.booking.dto.CheckoutRequestDto;
import com.bookmytree.platform.booking.dto.OrderResponseDto;
import com.bookmytree.platform.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponseDto> checkout(@Valid @RequestBody CheckoutRequestDto request) {
        OrderResponseDto order = bookingService.checkoutBranch(
                request.getUserId(),
                request.getBranchId(),
                request.getYieldPreference()
        );
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }
}