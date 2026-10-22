package com.bookmytree.platform.search.controller;

import com.bookmytree.platform.search.dto.FieldResponseDto;
import com.bookmytree.platform.search.service.FieldSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fields")
@RequiredArgsConstructor
public class FieldSearchController {

    private final FieldSearchService fieldSearchService;

    @GetMapping("/search")
    public ResponseEntity<List<FieldResponseDto>> searchFields(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "10.0") double radiusKm,
            @RequestParam(required = false) String species,
            @RequestParam(required = false) Double minRating
    ) {
        List<FieldResponseDto> fields = fieldSearchService.searchNearbyOrchards(lat, lng, radiusKm, species, minRating);
        return ResponseEntity.ok(fields);
    }
}