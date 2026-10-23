package com.bookmytree.platform.metaverse.entity;

import com.bookmytree.platform.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "soil_reports", indexes = {
        @Index(name = "idx_field_id", columnList = "fieldId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SoilReport extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "field_id", nullable = false)
    private UUID fieldId; // References the orchard Field ID

    @Column(nullable = false)
    private Double phLevel;

    @Column(nullable = false)
    private Double nitrogenContentKgPerHectare;

    @Column(nullable = false)
    private Double phosphorusContentKgPerHectare;

    @Column(nullable = false)
    private Double potassiumContentKgPerHectare;

    @Column(name = "soil_type", nullable = false)
    private String soilType; // e.g., "Alluvial", "Red Laterite", "Black Soil"

    @Column(name = "test_date", nullable = false)
    private LocalDate testDate;

    @Column(name = "terrain_asset_url")
    private String terrainAssetUrl; // 3D Metaverse asset model link
}