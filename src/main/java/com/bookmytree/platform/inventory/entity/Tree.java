package com.bookmytree.platform.inventory.entity;

import com.bookmytree.platform.common.entity.BaseAuditEntity;
import com.bookmytree.platform.search.entity.Field;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "trees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tree extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    @Column(nullable = false)
    private String species; // e.g., Alphonso Mango, Nagpur Orange

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HarvestStage stage = HarvestStage.FLOWERING;

    @Column(columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point coordinates;

    @OneToMany(mappedBy = "tree", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TreeBranch> branches = new ArrayList<>();

    public enum HarvestStage {
        FLOWERING, FRUIT_SET, MATURE, HARVESTED
    }
}