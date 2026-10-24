package com.bookmytree.platform.metaverse.entity;

import com.bookmytree.platform.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

@Entity
@Table(name = "virtual_farm_items", indexes = {
    @Index(name = "idx_virtual_user_id", columnList = "userId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VirtualFarmItem extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String cropSpecies; // e.g., "Alphonso Mango", "Sandalwood"

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point coordinates; // Real-world GPS anchor

    @Column(nullable = false)
    private String metaversePlotId; // e.g., "PLOT-SECTOR-7"

    @Column(nullable = false)
    private Integer growthStage = 1; // 1 to 5 scale
}