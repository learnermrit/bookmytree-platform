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
@Table(name = "user_adoption_records", indexes = {
        @Index(name = "idx_user_adoption_user", columnList = "user_id"),
        @Index(name = "idx_user_adoption_listing", columnList = "listing_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAdoptionRecord extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "listing_id", nullable = false)
    private UUID listingId;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "amount_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "points_awarded", nullable = false)
    private Integer pointsAwarded = 0;

    @Column(name = "carbon_offset_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal carbonOffsetKg = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdoptionStatus status = AdoptionStatus.ADOPTED;
}
