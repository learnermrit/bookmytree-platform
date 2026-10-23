package com.bookmytree.platform.ecommerce.controller;

import com.bookmytree.platform.ecommerce.dto.*;
import com.bookmytree.platform.ecommerce.entity.Product;
import com.bookmytree.platform.ecommerce.service.EcommerceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/store")
@RequiredArgsConstructor
public class EcommerceController {

    private final EcommerceService ecommerceService;

    @PostMapping("/products")
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductRequestDto request) {
        ProductResponseDto response = ecommerceService.createProduct(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponseDto>> browseProducts(
            @RequestParam(required = false) Product.ProductCategory category
    ) {
        List<ProductResponseDto> products = ecommerceService.browseProducts(category);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/cart/items")
    public ResponseEntity<String> addToCart(@Valid @RequestBody CartItemRequestDto request) {
        String message = ecommerceService.addToCart(request);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/checkout")
    public ResponseEntity<String> checkout(@Valid @RequestBody StoreCheckoutRequestDto request) {
        String receipt = ecommerceService.checkoutStore(request);
        return ResponseEntity.ok(receipt);
    }
}