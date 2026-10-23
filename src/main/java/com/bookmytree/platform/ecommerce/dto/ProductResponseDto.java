package com.bookmytree.platform.ecommerce.dto;

import com.bookmytree.platform.ecommerce.entity.Product;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ProductResponseDto {
    private UUID id;
    private UUID farmerId;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Product.ProductCategory category;
}