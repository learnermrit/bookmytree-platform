package com.bookmytree.platform.common.seed;

import com.bookmytree.platform.inventory.entity.Tree;
import com.bookmytree.platform.inventory.entity.TreeBranch;
import com.bookmytree.platform.inventory.repository.TreeBranchRepository;
import com.bookmytree.platform.inventory.repository.TreeRepository;
import com.bookmytree.platform.search.entity.Field;
import com.bookmytree.platform.search.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@Profile("seed")
@RequiredArgsConstructor
public class DatabaseSeeder implements ApplicationRunner {

    private static final UUID FARMER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final FieldRepository fieldRepository;
    private final TreeRepository treeRepository;
    private final TreeBranchRepository treeBranchRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (fieldRepository.count() > 0) {
            log.info("Database already contains fields. Skipping seeding.");
            return;
        }

        Field field = new Field();
        field.setName("Sunburst Plot");
        field.setFarmerId(FARMER_ID);
        field.setLocation(createPoint(19.0760, 72.8777));
        field.setBoundaryPolygon(createPolygon(19.0760, 72.8777));
        field.setRating(4.8);
        field.setTotalTreesCount(1);
        field = fieldRepository.saveAndFlush(field);

        Tree tree = new Tree();
        tree.setField(field);
        tree.setSpecies("Sweet Orange");
        tree.setStage(Tree.HarvestStage.MATURE);
        tree.setCoordinates(createPoint(19.0760, 72.8777));
        tree = treeRepository.saveAndFlush(tree);

        List<TreeBranch> branches = List.of(
                buildBranch(tree, "North-Main-Branch-01", 18.0, new BigDecimal("420"), true),
                buildBranch(tree, "East-Canopy-Branch", 14.0, new BigDecimal("360"), true),
                buildBranch(tree, "South-Fruit-Branch", 12.0, new BigDecimal("320"), false),
                buildBranch(tree, "West-Seasonal-Branch", 22.0, new BigDecimal("510"), true));
        treeBranchRepository.saveAllAndFlush(branches);

        log.info("Seeded demo field and tree for farmer {}", FARMER_ID);
    }

    private TreeBranch buildBranch(Tree tree, String branchIdentifier, double estimatedYieldKg,
            BigDecimal price, boolean available) {
        TreeBranch branch = new TreeBranch();
        branch.setTree(tree);
        branch.setBranchIdentifier(branchIdentifier);
        branch.setEstimatedYieldKg(estimatedYieldKg);
        branch.setPrice(price);
        branch.setAvailable(available);
        return branch;
    }

    private Point createPoint(double latitude, double longitude) {
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    private Polygon createPolygon(double latitude, double longitude) {
        Coordinate[] coordinates = new Coordinate[] {
                new Coordinate(longitude - 0.003, latitude - 0.003),
                new Coordinate(longitude + 0.003, latitude - 0.003),
                new Coordinate(longitude + 0.003, latitude + 0.003),
                new Coordinate(longitude - 0.003, latitude + 0.003),
                new Coordinate(longitude - 0.003, latitude - 0.003)
        };
        return geometryFactory.createPolygon(coordinates);
    }
}
