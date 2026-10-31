package com.bookmytree.platform.inventory.service;

import com.bookmytree.platform.auth.entity.User;
import com.bookmytree.platform.booking.entity.Order;
import com.bookmytree.platform.booking.repository.OrderRepository;
import com.bookmytree.platform.common.exception.BadRequestException;
import com.bookmytree.platform.inventory.dto.BulkCreateTreesRequestDto;
import com.bookmytree.platform.inventory.dto.BulkUpdateTreesRequestDto;
import com.bookmytree.platform.inventory.entity.Tree;
import com.bookmytree.platform.inventory.entity.TreeBranch;
import com.bookmytree.platform.inventory.repository.TreeBranchRepository;
import com.bookmytree.platform.inventory.repository.TreeRepository;
import com.bookmytree.platform.search.entity.Field;
import com.bookmytree.platform.search.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FarmerInventoryService {

    private final FieldRepository fieldRepository;
    private final TreeRepository treeRepository;
    private final TreeBranchRepository treeBranchRepository;
    private final OrderRepository orderRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional
    public List<Tree> bulkCreateTrees(Field field, User farmer, BulkCreateTreesRequestDto request) {
        if (!field.getFarmerId().equals(farmer.getId())) {
            throw new BadRequestException("This field does not belong to the current farmer.");
        }

        Tree.HarvestStage stage = parseStage(request.getStage());
        int count = request.getCount() == null ? 1 : request.getCount();
        if (count < 1) {
            throw new BadRequestException("Tree count must be 1 or greater.");
        }

        List<Tree> createdTrees = new ArrayList<>();
        Point basePoint = field.getLocation() != null
                ? field.getLocation()
                : createPoint(request.getLatitude(), request.getLongitude());

        for (int index = 0; index < count; index++) {
            Tree tree = new Tree();
            tree.setField(field);
            tree.setSpecies(request.getSpecies().trim());
            tree.setStage(stage);
            tree.setCoordinates(
                    createOffsetPoint(basePoint, index, count, request.getLatitude(), request.getLongitude()));

            Tree savedTree = treeRepository.save(tree);

            if (request.isAddDefaultBranch()) {
                TreeBranch branch = new TreeBranch();
                branch.setTree(savedTree);
                branch.setBranchIdentifier(buildBranchName(request.getBranchIdentifier(), index + 1));
                branch.setEstimatedYieldKg(request.getInitialEstimatedYieldKg() != null
                        ? request.getInitialEstimatedYieldKg()
                        : 18.0);
                branch.setPrice(request.getBasePrice() != null ? request.getBasePrice() : new BigDecimal("420.00"));
                branch.setAvailable(true);
                treeBranchRepository.save(branch);
                savedTree.getBranches().add(branch);
            }

            createdTrees.add(savedTree);
        }

        field.setTotalTreesCount((field.getTotalTreesCount() == null ? 0 : field.getTotalTreesCount()) + count);
        fieldRepository.save(field);

        return createdTrees;
    }

    @Transactional
    public List<Tree> bulkUpdateTrees(Field field, User farmer, BulkUpdateTreesRequestDto request) {
        if (!field.getFarmerId().equals(farmer.getId())) {
            throw new BadRequestException("This field does not belong to the current farmer.");
        }

        if (request.getTreeIds() == null || request.getTreeIds().isEmpty()) {
            throw new BadRequestException("At least one tree ID is required.");
        }

        List<Tree> trees = treeRepository.findAllById(request.getTreeIds());
        List<Tree> matchedTrees = trees.stream()
                .filter(tree -> tree.getField() != null && tree.getField().getId().equals(field.getId()))
                .toList();

        if (matchedTrees.size() != request.getTreeIds().size()) {
            throw new BadRequestException("One or more selected trees were not found in this field.");
        }

        if (request.getStage() != null && !request.getStage().isBlank()) {
            Tree.HarvestStage updatedStage = parseStage(request.getStage());
            matchedTrees.forEach(tree -> tree.setStage(updatedStage));
        }

        if (request.getBasePrice() != null) {
            matchedTrees.forEach(tree -> tree.getBranches().forEach(branch -> branch.setPrice(request.getBasePrice())));
            treeBranchRepository.saveAll(matchedTrees.stream()
                    .flatMap(tree -> tree.getBranches().stream())
                    .toList());
        }

        if (request.isAddBranch()) {
            String branchIdentifier = request.getBranchIdentifier() == null || request.getBranchIdentifier().isBlank()
                    ? "North-Canopy-Branch"
                    : request.getBranchIdentifier();

            double estimatedYieldKg = request.getEstimatedYieldKg() != null ? request.getEstimatedYieldKg() : 18.0;
            BigDecimal price = request.getBasePrice() != null ? request.getBasePrice() : new BigDecimal("420.00");

            List<TreeBranch> newBranches = new ArrayList<>();
            for (Tree tree : matchedTrees) {
                TreeBranch branch = new TreeBranch();
                branch.setTree(tree);
                branch.setBranchIdentifier(branchIdentifier);
                branch.setEstimatedYieldKg(estimatedYieldKg);
                branch.setPrice(price);
                branch.setAvailable(true);
                newBranches.add(branch);
                tree.getBranches().add(branch);
            }

            if (!newBranches.isEmpty()) {
                treeBranchRepository.saveAll(newBranches);
            }
        }

        return treeRepository.saveAll(matchedTrees);
    }

    @Transactional(readOnly = true)
    public boolean isFieldBooked(Field field) {
        if (field == null) {
            return false;
        }

        List<Tree> trees = treeRepository.findByFieldId(field.getId());
        return trees.stream().anyMatch(this::isTreeBooked);
    }

    @Transactional(readOnly = true)
    public boolean isTreeBooked(Tree tree) {
        if (tree == null) {
            return false;
        }

        if (orderRepository.existsByItemTypeAndItemId(Order.ItemType.WHOLE_TREE, tree.getId())
                || orderRepository.existsByItemTypeAndItemId(Order.ItemType.PERCENTAGE_SHARE, tree.getId())) {
            return true;
        }

        List<TreeBranch> branches = treeBranchRepository.findByTreeId(tree.getId());
        return branches.stream().anyMatch(branch -> !branch.isAvailable()
                || orderRepository.existsByItemTypeAndItemId(Order.ItemType.BRANCH, branch.getId()));
    }

    @Transactional(readOnly = true)
    public boolean isBranchBooked(TreeBranch branch) {
        if (branch == null) {
            return false;
        }

        return !branch.isAvailable()
                || orderRepository.existsByItemTypeAndItemId(Order.ItemType.BRANCH, branch.getId());
    }

    private Tree.HarvestStage parseStage(String stage) {
        try {
            return Tree.HarvestStage.valueOf(stage.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid tree stage: " + stage);
        }
    }

    private Point createOffsetPoint(Point basePoint, int index, int count, Double latitudeOverride,
            Double longitudeOverride) {
        double baseLat = basePoint != null && basePoint.getY() != 0 ? basePoint.getY()
                : (latitudeOverride != null ? latitudeOverride : 19.0760);
        double baseLon = basePoint != null && basePoint.getX() != 0 ? basePoint.getX()
                : (longitudeOverride != null ? longitudeOverride : 72.8777);

        int grid = Math.max(1, (int) Math.ceil(Math.sqrt(count)));
        int row = index / grid;
        int col = index % grid;
        double latOffset = (row - (grid - 1) / 2.0) * 0.00055;
        double lonOffset = (col - (grid - 1) / 2.0) * 0.00065;

        if (count == 1) {
            latOffset = 0.0;
            lonOffset = 0.0;
        }

        return createPoint(baseLat + latOffset, baseLon + lonOffset);
    }

    private Point createPoint(double latitude, double longitude) {
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    private String buildBranchName(String baseIdentifier, int sequence) {
        String cleaned = baseIdentifier == null || baseIdentifier.isBlank() ? "North-Canopy-Branch"
                : baseIdentifier.trim();
        return cleaned + "-" + sequence;
    }
}
