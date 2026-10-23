package com.bookmytree.platform.ecommerce.entity;

import com.bookmytree.platform.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_farmer_id", columnList = "farmerId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "farmer_id", nullable = false)
    private UUID farmerId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;

    public enum ProductCategory {
        RAW_PRODUCE, HONEY, OILS, FARMING_INPUTS
    }
}