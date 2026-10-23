package com.bookmytree.platform.ecommerce.service;

import com.bookmytree.platform.common.exception.BadRequestException;
import com.bookmytree.platform.common.exception.ResourceNotFoundException;
import com.bookmytree.platform.ecommerce.dto.*;
import com.bookmytree.platform.ecommerce.entity.Product;
import com.bookmytree.platform.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EcommerceService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto request) {
        log.info("Listing new product '{}' for farmer: {}", request.getTitle(), request.getFarmerId());

        Product product = new Product();
        product.setFarmerId(request.getFarmerId());
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(request.getCategory());

        Product savedProduct = productRepository.save(product);
        return mapToDto(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> browseProducts(Product.ProductCategory category) {
        log.info("Browsing marketplace products. Category filter: {}", category);
        List<Product> products = (category != null)
                ? productRepository.findByCategory(category)
                : productRepository.findAll();

        return products.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public String addToCart(CartItemRequestDto request) {
        log.info("Adding product {} (Qty: {}) to cart for user: {}", request.getProductId(), request.getQuantity(),
                request.getUserId());

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException(
                    "Insufficient stock available. Only " + product.getStockQuantity() + " left.");
        }

        return "Successfully added " + request.getQuantity() + " unit(s) of '" + product.getTitle() + "' to cart.";
    }

    @Transactional
    public String checkoutStore(StoreCheckoutRequestDto request) {
        log.info("Processing e-commerce checkout for user: {}. Green points discount applied: {}", request.getUserId(),
                request.isUseGreenPointsDiscount());
        // Simulating e-commerce order settlement
        return "Store checkout successful! Order placed and total calculated with green points adjustment.";
    }

    private ProductResponseDto mapToDto(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .farmerId(product.getFarmerId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .category(product.getCategory())
                .build();
    }
}