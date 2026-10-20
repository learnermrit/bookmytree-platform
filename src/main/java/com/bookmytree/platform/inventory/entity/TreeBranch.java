package com.bookmytree.platform.inventory.entity;

import com.bookmytree.platform.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tree_branches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TreeBranch extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tree_id", nullable = false)
    private Tree tree;

    @Column(nullable = false)
    private String branchIdentifier; // e.g., "North-Main-Branch-01"

    @Column(name = "estimated_yield_kg", nullable = false)
    private Double estimatedYieldKg;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean isAvailable = true;
}