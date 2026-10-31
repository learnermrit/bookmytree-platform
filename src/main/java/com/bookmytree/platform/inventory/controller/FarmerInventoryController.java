package com.bookmytree.platform.inventory.controller;

import com.bookmytree.platform.auth.entity.User;
import com.bookmytree.platform.auth.repository.UserRepository;
import com.bookmytree.platform.booking.entity.Order;
import com.bookmytree.platform.booking.repository.OrderRepository;
import com.bookmytree.platform.common.exception.BadRequestException;
import com.bookmytree.platform.common.exception.ResourceNotFoundException;
import com.bookmytree.platform.inventory.dto.BulkCreateTreesRequestDto;
import com.bookmytree.platform.inventory.dto.BulkUpdateTreesRequestDto;
import com.bookmytree.platform.inventory.dto.CreateFieldRequestDto;
import com.bookmytree.platform.inventory.dto.CreateTreeBranchRequestDto;
import com.bookmytree.platform.inventory.dto.CreateTreeRequestDto;
import com.bookmytree.platform.inventory.dto.RenameFieldRequestDto;
import com.bookmytree.platform.inventory.dto.RenameTreeBranchRequestDto;
import com.bookmytree.platform.inventory.dto.RenameTreeRequestDto;
import com.bookmytree.platform.inventory.dto.TreeBranchResponseDto;
import com.bookmytree.platform.inventory.dto.TreeResponseDto;
import com.bookmytree.platform.inventory.entity.Tree;
import com.bookmytree.platform.inventory.entity.TreeBranch;
import com.bookmytree.platform.inventory.repository.TreeBranchRepository;
import com.bookmytree.platform.inventory.repository.TreeRepository;
import com.bookmytree.platform.inventory.service.FarmerInventoryService;
import com.bookmytree.platform.search.dto.FieldResponseDto;
import com.bookmytree.platform.search.entity.Field;
import com.bookmytree.platform.search.repository.FieldRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/farmers")
@RequiredArgsConstructor
public class FarmerInventoryController {

    private final FieldRepository fieldRepository;
    private final TreeRepository treeRepository;
    private final TreeBranchRepository treeBranchRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final FarmerInventoryService farmerInventoryService;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @GetMapping("/fields")
    public ResponseEntity<List<FieldResponseDto>> getMyFields() {
        User farmer = getCurrentFarmer();
        List<Field> fields = fieldRepository.findByFarmerId(farmer.getId());
        return ResponseEntity.ok(fields.stream().map(this::toFieldResponse).toList());
    }

    @PostMapping("/fields")
    public ResponseEntity<FieldResponseDto> createField(@Valid @RequestBody CreateFieldRequestDto request) {
        User farmer = getCurrentFarmer();

        Field field = new Field();
        field.setName(request.getName().trim());
        field.setFarmerId(farmer.getId());
        field.setLocation(createPoint(request.getLatitude(), request.getLongitude()));
        field.setBoundaryPolygon(createPolygon(request.getLatitude(), request.getLongitude()));
        field.setRating(0.0);
        field.setTotalTreesCount(0);

        Field saved = fieldRepository.save(field);
        return ResponseEntity.status(HttpStatus.CREATED).body(toFieldResponse(saved));
    }

    @PatchMapping("/fields/{fieldId}")
    public ResponseEntity<FieldResponseDto> renameField(@PathVariable UUID fieldId,
            @Valid @RequestBody RenameFieldRequestDto request) {
        User farmer = getCurrentFarmer();
        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found with ID: " + fieldId));

        if (!field.getFarmerId().equals(farmer.getId())) {
            throw new BadRequestException("This field does not belong to the current farmer.");
        }

        field.setName(request.getName().trim());
        Field saved = fieldRepository.save(field);
        return ResponseEntity.ok(toFieldResponse(saved));
    }

    @DeleteMapping("/fields/{fieldId}")
    public ResponseEntity<String> deleteField(@PathVariable UUID fieldId) {
        User farmer = getCurrentFarmer();
        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found with ID: " + fieldId));

        if (!field.getFarmerId().equals(farmer.getId())) {
            throw new BadRequestException("This field does not belong to the current farmer.");
        }

        if (farmerInventoryService.isFieldBooked(field)) {
            throw new BadRequestException("Cannot delete this field because one or more trees are already booked.");
        }

        List<Tree> trees = treeRepository.findByFieldId(fieldId);
        for (Tree tree : trees) {
            List<TreeBranch> branches = treeBranchRepository.findByTreeId(tree.getId());
            if (!branches.isEmpty()) {
                treeBranchRepository.deleteAll(branches);
            }
            treeRepository.delete(tree);
        }

        fieldRepository.delete(field);
        return ResponseEntity.ok("Field and all related trees were deleted successfully.");
    }

    @GetMapping("/fields/{fieldId}/trees")
    public ResponseEntity<List<TreeResponseDto>> getTreesForField(@PathVariable UUID fieldId) {
        User farmer = getCurrentFarmer();
        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found with ID: " + fieldId));

        if (!field.getFarmerId().equals(farmer.getId())) {
            throw new BadRequestException("This field does not belong to the current farmer.");
        }

        List<Tree> trees = treeRepository.findByFieldId(fieldId);
        return ResponseEntity.ok(trees.stream().map(this::toTreeResponse).toList());
    }

    @PatchMapping("/trees/{treeId}")
    public ResponseEntity<TreeResponseDto> renameTree(@PathVariable UUID treeId,
            @Valid @RequestBody RenameTreeRequestDto request) {
        User farmer = getCurrentFarmer();
        Tree tree = treeRepository.findById(treeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tree not found with ID: " + treeId));

        if (!tree.getField().getFarmerId().equals(farmer.getId())) {
            throw new BadRequestException("This tree does not belong to the current farmer.");
        }

        tree.setSpecies(request.getSpecies().trim());
        if (request.getStage() != null && !request.getStage().isBlank()) {
            tree.setStage(Tree.HarvestStage.valueOf(request.getStage().trim().toUpperCase(Locale.ROOT)));
        }

        Tree saved = treeRepository.save(tree);
        return ResponseEntity.ok(toTreeResponse(saved));
    }

    @DeleteMapping("/trees/{treeId}")
    public ResponseEntity<String> deleteTree(@PathVariable UUID treeId) {
        User farmer = getCurrentFarmer();
        Tree tree = treeRepository.findById(treeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tree not found with ID: " + treeId));

        if (!tree.getField().getFarmerId().equals(farmer.getId())) {
            throw new BadRequestException("This tree does not belong to the current farmer.");
        }

        if (farmerInventoryService.isTreeBooked(tree)) {
            throw new BadRequestException("Cannot delete this tree because it has an active booking.");
        }

        List<TreeBranch> branches = treeBranchRepository.findByTreeId(treeId);
        if (!branches.isEmpty()) {
            treeBranchRepository.deleteAll(branches);
        }
        treeRepository.delete(tree);

        Field field = tree.getField();
        if (field != null && field.getTotalTreesCount() != null && field.getTotalTreesCount() > 0) {
            field.setTotalTreesCount(field.getTotalTreesCount() - 1);
            fieldRepository.save(field);
        }

        return ResponseEntity.ok("Tree deleted successfully.");
    }

    @PostMapping("/fields/{fieldId}/trees")
    public ResponseEntity<TreeResponseDto> createTree(@PathVariable UUID fieldId,
            @Valid @RequestBody CreateTreeRequestDto request) {
        User farmer = getCurrentFarmer();
        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found with ID: " + fieldId));

        if (!field.getFarmerId().equals(farmer.getId())) {
            throw new BadRequestException("This field does not belong to the current farmer.");
        }

        Tree.HarvestStage stage;
        try {
            stage = Tree.HarvestStage.valueOf(request.getStage().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid tree stage: " + request.getStage());
        }

        Tree tree = new Tree();
        tree.setField(field);
        tree.setSpecies(request.getSpecies().trim());
        tree.setStage(stage);
        tree.setCoordinates(createPoint(request.getLatitude(), request.getLongitude()));

        Tree saved = treeRepository.save(tree);
        field.setTotalTreesCount(field.getTotalTreesCount() == null ? 1 : field.getTotalTreesCount() + 1);
        fieldRepository.save(field);

        return ResponseEntity.status(HttpStatus.CREATED).body(toTreeResponse(saved));
    }

    @PostMapping("/fields/{fieldId}/trees/bulk")
    public ResponseEntity<List<TreeResponseDto>> bulkCreateTrees(@PathVariable UUID fieldId,
            @Valid @RequestBody BulkCreateTreesRequestDto request) {
        User farmer = getCurrentFarmer();
        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found with ID: " + fieldId));

        List<Tree> trees = farmerInventoryService.bulkCreateTrees(field, farmer, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trees.stream().map(this::toTreeResponse).toList());
    }

    @PatchMapping("/fields/{fieldId}/trees/bulk-update")
    public ResponseEntity<List<TreeResponseDto>> bulkUpdateTrees(@PathVariable UUID fieldId,
            @Valid @RequestBody BulkUpdateTreesRequestDto request) {
        User farmer = getCurrentFarmer();
        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found with ID: " + fieldId));

        List<Tree> updatedTrees = farmerInventoryService.bulkUpdateTrees(field, farmer, request);
        return ResponseEntity.ok(updatedTrees.stream().map(this::toTreeResponse).toList());
    }

    @PostMapping("/trees/{treeId}/branches")
    public ResponseEntity<List<TreeBranchResponseDto>> addBranches(@PathVariable UUID treeId,
            @Valid @RequestBody List<CreateTreeBranchRequestDto> request) {
        User farmer = getCurrentFarmer();
        Tree tree = treeRepository.findById(treeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tree not found with ID: " + treeId));

        if (!tree.getField().getFarmerId().equals(farmer.getId())) {
            throw new BadRequestException("This tree does not belong to the current farmer.");
        }

        List<TreeBranch> branches = request.stream().map(item -> {
            TreeBranch branch = new TreeBranch();
            branch.setTree(tree);
            branch.setBranchIdentifier(item.getBranchIdentifier().trim());
            branch.setEstimatedYieldKg(item.getEstimatedYieldKg());
            branch.setPrice(item.getPrice());
            branch.setAvailable(item.isAvailable());
            return branch;
        }).toList();

        List<TreeBranch> saved = treeBranchRepository.saveAll(branches);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved.stream().map(this::toBranchResponse).toList());
    }

    @PatchMapping("/trees/{treeId}/branches/{branchId}")
    public ResponseEntity<TreeBranchResponseDto> renameBranch(@PathVariable UUID treeId,
            @PathVariable UUID branchId,
            @Valid @RequestBody RenameTreeBranchRequestDto request) {
        User farmer = getCurrentFarmer();
        Tree tree = treeRepository.findById(treeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tree not found with ID: " + treeId));

        if (!tree.getField().getFarmerId().equals(farmer.getId())) {
            throw new BadRequestException("This tree does not belong to the current farmer.");
        }

        TreeBranch branch = treeBranchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Tree branch not found with ID: " + branchId));

        if (!branch.getTree().getId().equals(treeId)) {
            throw new BadRequestException("This branch does not belong to the selected tree.");
        }

        if (farmerInventoryService.isBranchBooked(branch)) {
            throw new BadRequestException("Cannot rename this branch because it is already booked.");
        }

        branch.setBranchIdentifier(request.getBranchIdentifier().trim());
        TreeBranch saved = treeBranchRepository.save(branch);
        return ResponseEntity.ok(toBranchResponse(saved));
    }

    @DeleteMapping("/trees/{treeId}/branches/{branchId}")
    public ResponseEntity<String> deleteBranch(@PathVariable UUID treeId,
            @PathVariable UUID branchId) {
        User farmer = getCurrentFarmer();
        Tree tree = treeRepository.findById(treeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tree not found with ID: " + treeId));

        if (!tree.getField().getFarmerId().equals(farmer.getId())) {
            throw new BadRequestException("This tree does not belong to the current farmer.");
        }

        TreeBranch branch = treeBranchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Tree branch not found with ID: " + branchId));

        if (!branch.getTree().getId().equals(treeId)) {
            throw new BadRequestException("This branch does not belong to the selected tree.");
        }

        if (farmerInventoryService.isBranchBooked(branch)) {
            throw new BadRequestException("Cannot delete this branch because it is already booked.");
        }

        treeBranchRepository.delete(branch);
        return ResponseEntity.ok("Branch deleted successfully.");
    }

    private FieldResponseDto toFieldResponse(Field field) {
        return FieldResponseDto.builder()
                .id(field.getId())
                .name(field.getName())
                .farmerId(field.getFarmerId())
                .latitude(field.getLocation() != null ? field.getLocation().getY() : null)
                .longitude(field.getLocation() != null ? field.getLocation().getX() : null)
                .rating(field.getRating())
                .totalTreesCount(field.getTotalTreesCount())
                .build();
    }

    private TreeResponseDto toTreeResponse(Tree tree) {
        UUID bookingId = orderRepository
                .findTopByItemTypeAndItemIdOrderByCreatedAtDesc(Order.ItemType.WHOLE_TREE, tree.getId())
                .or(() -> orderRepository
                        .findTopByItemTypeAndItemIdOrderByCreatedAtDesc(Order.ItemType.PERCENTAGE_SHARE, tree.getId()))
                .map(Order::getId)
                .orElse(null);

        return TreeResponseDto.builder()
                .id(tree.getId())
                .fieldId(tree.getField() != null ? tree.getField().getId() : null)
                .bookingId(bookingId)
                .booked(bookingId != null)
                .species(tree.getSpecies())
                .stage(tree.getStage() != null ? tree.getStage().name() : null)
                .latitude(tree.getCoordinates() != null ? tree.getCoordinates().getY() : null)
                .longitude(tree.getCoordinates() != null ? tree.getCoordinates().getX() : null)
                .branches(tree.getBranches() == null ? List.of()
                        : tree.getBranches().stream().map(this::toBranchResponse).toList())
                .build();
    }

    private TreeBranchResponseDto toBranchResponse(TreeBranch branch) {
        UUID bookingId = orderRepository
                .findTopByItemTypeAndItemIdOrderByCreatedAtDesc(Order.ItemType.BRANCH, branch.getId())
                .map(Order::getId)
                .orElse(null);

        return TreeBranchResponseDto.builder()
                .id(branch.getId())
                .bookingId(bookingId)
                .booked(bookingId != null || !branch.isAvailable())
                .branchIdentifier(branch.getBranchIdentifier())
                .estimatedYieldKg(branch.getEstimatedYieldKg())
                .price(branch.getPrice())
                .available(branch.isAvailable())
                .build();
    }

    private User getCurrentFarmer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new BadRequestException("User is not authenticated.");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(
                        () -> new ResourceNotFoundException("User not found with email: " + authentication.getName()));

        if (user.getRole() != User.Role.FARMER) {
            throw new BadRequestException("Only farmer accounts can manage fields and trees.");
        }

        return user;
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
