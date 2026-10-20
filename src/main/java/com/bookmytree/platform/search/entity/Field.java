package com.bookmytree.platform.search.entity;

import com.bookmytree.platform.common.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.UUID;

@Entity
@Table(name = "fields", indexes = {
        @Index(name = "idx_field_location", columnList = "location"),
        @Index(name = "idx_farmer_id", columnList = "farmer_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Field extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "farmer_id", nullable = false)
    private UUID farmerId;

    @Column(columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point location; // Latitude & Longitude point

    @Column(columnDefinition = "geometry(Polygon, 4326)", nullable = false)
    private Polygon boundaryPolygon; // Orchard perimeter

    private Double rating;

    @Column(name = "total_trees_count")
    private Integer totalTreesCount = 0;
}