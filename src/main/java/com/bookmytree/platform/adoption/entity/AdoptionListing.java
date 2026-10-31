package com.bookmytree.platform.adoption.entity;

import com.bookmytree.platform.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "adoption_listings", indexes = {
        @Index(name = "idx_adoption_listing_farmer", columnList = "farmer_id"),
        @Index(name = "idx_adoption_listing_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdoptionListing extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "farmer_id", nullable = false)
    private UUID farmerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdoptionCategory category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String name;

    @Column(name = "quantity_available", nullable = false)
    private Integer quantityAvailable = 0;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "points_reward", nullable = false)
    private Integer pointsReward = 0;

    @Column(name = "carbon_offset_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal carbonOffsetKg = BigDecimal.ZERO;

    @Column(length = 1500)
    private String description;

    private String location;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "unit_label")
    private String unitLabel = "units";

    @Column(nullable = false)
    private boolean active = true;
}
