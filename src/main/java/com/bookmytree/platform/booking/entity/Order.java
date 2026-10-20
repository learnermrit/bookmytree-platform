package com.bookmytree.platform.booking.entity;

import com.bookmytree.platform.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "item_id", nullable = false)
    private UUID itemId; // References Tree ID or TreeBranch ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType itemType;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING_LOCK;

    @Enumerated(EnumType.STRING)
    @Column(name = "yield_preference", nullable = false)
    private YieldPreference yieldPreference = YieldPreference.PARCEL_DELIVERY;

    public enum ItemType {
        WHOLE_TREE, BRANCH, PERCENTAGE_SHARE
    }

    public enum OrderStatus {
        PENDING_LOCK, PAYMENT_PENDING, CONFIRMED, FAILED, CANCELLED
    }

    public enum YieldPreference {
        PARCEL_DELIVERY, RESELL_PROFIT_SHARE, DONATION
    }
}