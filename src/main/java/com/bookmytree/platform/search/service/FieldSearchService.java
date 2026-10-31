package com.bookmytree.platform.search.service;

import com.bookmytree.platform.auth.entity.User;
import com.bookmytree.platform.auth.repository.UserRepository;
import com.bookmytree.platform.booking.entity.Order;
import com.bookmytree.platform.booking.repository.OrderRepository;
import com.bookmytree.platform.inventory.entity.Tree;
import com.bookmytree.platform.inventory.entity.TreeBranch;
import com.bookmytree.platform.inventory.repository.TreeRepository;
import com.bookmytree.platform.search.dto.FieldResponseDto;
import com.bookmytree.platform.search.entity.Field;
import com.bookmytree.platform.search.repository.FieldRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FieldSearchService {

    private final FieldRepository fieldRepository;
    private final TreeRepository treeRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<FieldResponseDto> searchNearbyOrchards(double latitude, double longitude, double radiusKm,
            String species, Double minRating) {
        double radiusInMeters = radiusKm * 1000.0;
        log.info("Executing spatial proximity search for lat: {}, lng: {}, radius: {}m", latitude, longitude,
                radiusInMeters);

        List<Field> fields = fieldRepository.findNearbyFields(longitude, latitude, radiusInMeters, species, minRating);

        return fields.stream().map(field -> FieldResponseDto.builder()
                .id(field.getId())
                .name(field.getName())
                .farmerId(field.getFarmerId())
                .latitude(field.getLocation() != null ? field.getLocation().getY() : null)
                .longitude(field.getLocation() != null ? field.getLocation().getX() : null)
                .rating(field.getRating())
                .totalTreesCount(field.getTotalTreesCount())
                .build()).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FieldResponseDto> searchOrchards(String query, Double latitude, Double longitude, Double radiusKm,
            String species, Double minRating) {
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isBlank()) {
            if (latitude != null && longitude != null && radiusKm != null) {
                return searchNearbyOrchards(latitude, longitude, radiusKm, species, minRating);
            }
            return fieldRepository.findAll().stream().map(this::toFieldResponse).toList();
        }

        List<Field> baseFields = (latitude != null && longitude != null && radiusKm != null)
                ? fieldRepository.findNearbyFields(longitude, latitude, radiusKm * 1000.0, species, minRating)
                : fieldRepository.findAll();

        String needle = normalizedQuery.toLowerCase();
        List<Field> matches = new ArrayList<>();

        for (Field field : baseFields) {
            boolean fieldMatch = field.getName() != null && field.getName().toLowerCase().contains(needle);
            boolean farmerMatch = false;
            User farmer = userRepository.findById(field.getFarmerId()).orElse(null);
            if (farmer != null && farmer.getFullName() != null) {
                farmerMatch = farmer.getFullName().toLowerCase().contains(needle);
            }

            boolean treeMatch = false;
            if (species == null || species.isBlank() || species.equalsIgnoreCase("ALL")) {
                List<Tree> trees = treeRepository.findByFieldId(field.getId());
                treeMatch = trees.stream().anyMatch(
                        tree -> tree.getSpecies() != null && tree.getSpecies().toLowerCase().contains(needle));
            } else {
                treeMatch = treeRepository.findByFieldId(field.getId()).stream()
                        .anyMatch(tree -> tree.getSpecies() != null && tree.getSpecies().equalsIgnoreCase(species)
                                && (tree.getSpecies() == null || true));
            }

            if (fieldMatch || farmerMatch || treeMatch) {
                matches.add(field);
            }
        }

        return matches.stream().map(this::toFieldResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<Tree> getTreesForField(UUID fieldId) {
        if (!fieldRepository.existsById(fieldId)) {
            throw new EntityNotFoundException("Field not found with ID: " + fieldId);
        }

        return treeRepository.findByFieldId(fieldId);
    }

    public UUID findLatestBookingIdForTree(Tree tree) {
        if (tree == null)
            return null;
        return orderRepository.findTopByItemTypeAndItemIdOrderByCreatedAtDesc(Order.ItemType.WHOLE_TREE, tree.getId())
                .or(() -> orderRepository
                        .findTopByItemTypeAndItemIdOrderByCreatedAtDesc(Order.ItemType.PERCENTAGE_SHARE, tree.getId()))
                .map(Order::getId)
                .orElse(null);
    }

    public UUID findLatestBookingIdForBranch(TreeBranch branch) {
        if (branch == null)
            return null;
        return orderRepository.findTopByItemTypeAndItemIdOrderByCreatedAtDesc(Order.ItemType.BRANCH, branch.getId())
                .map(Order::getId)
                .orElse(null);
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
}