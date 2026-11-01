package com.bookmytree.platform.search.controller;

import com.bookmytree.platform.adoption.dto.AdoptionListingResponseDto;
import com.bookmytree.platform.adoption.dto.CreateAdoptionListingRequestDto;
import com.bookmytree.platform.adoption.service.AdoptionService;
import com.bookmytree.platform.inventory.entity.Tree;
import com.bookmytree.platform.search.dto.FieldResponseDto;
import com.bookmytree.platform.search.service.FieldSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fields")
@RequiredArgsConstructor
public class FieldSearchController {

    private final FieldSearchService fieldSearchService;
    private final AdoptionService adoptionService;

    @GetMapping("/search")
    public ResponseEntity<?> searchFields(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "10.0") double radiusKm,
            @RequestParam(required = false) String species,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "false") boolean forAdoption,
            @RequestParam(required = false) String category) {
        if (forAdoption) {
            List<AdoptionListingResponseDto> listings = adoptionService.getActiveListings();
            if (category != null && !category.isBlank()) {
                listings = listings.stream()
                        .filter(listing -> listing.getCategory() != null
                                && listing.getCategory().name().equalsIgnoreCase(category))
                        .toList();
            }
            return ResponseEntity.ok(listings);
        }

        List<FieldResponseDto> fields = fieldSearchService.searchOrchards(query, lat, lng, radiusKm, species,
                minRating);
        return ResponseEntity.ok(fields);
    }

    @PostMapping("/adoption-listing")
    public ResponseEntity<AdoptionListingResponseDto> createAdoptionListing(
            @Valid @RequestBody CreateAdoptionListingRequestDto request) {
        return new ResponseEntity<>(adoptionService.createListing(request), HttpStatus.CREATED);
    }

    @GetMapping("/{fieldId}/trees")
    public ResponseEntity<List<Map<String, Object>>> getFieldTrees(@PathVariable UUID fieldId) {
        List<Tree> trees = fieldSearchService.getTreesForField(fieldId);

        List<Map<String, Object>> payload = trees.stream().map(tree -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", tree.getId());
            item.put("fieldId", tree.getField() != null ? tree.getField().getId() : null);
            item.put("species", tree.getSpecies());
            item.put("stage", tree.getStage() != null ? tree.getStage().name() : null);
            item.put("latitude", tree.getCoordinates() != null ? tree.getCoordinates().getY() : null);
            item.put("longitude", tree.getCoordinates() != null ? tree.getCoordinates().getX() : null);
            item.put("bookingId", fieldSearchService.findLatestBookingIdForTree(tree));
            item.put("booked", fieldSearchService.findLatestBookingIdForTree(tree) != null);

            List<Map<String, Object>> branches = tree.getBranches() == null ? List.of()
                    : tree.getBranches().stream().map(branch -> {
                        Map<String, Object> branchMap = new HashMap<>();
                        branchMap.put("id", branch.getId());
                        branchMap.put("branchIdentifier", branch.getBranchIdentifier());
                        branchMap.put("estimatedYieldKg", branch.getEstimatedYieldKg());
                        branchMap.put("price", branch.getPrice());
                        branchMap.put("available", branch.isAvailable());
                        branchMap.put("bookingId", fieldSearchService.findLatestBookingIdForBranch(branch));
                        branchMap.put("booked", !branch.isAvailable()
                                || fieldSearchService.findLatestBookingIdForBranch(branch) != null);
                        return branchMap;
                    }).toList();

            item.put("branches", branches);
            return item;
        }).toList();

        return ResponseEntity.ok(payload);
    }
}