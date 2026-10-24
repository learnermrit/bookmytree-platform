package com.bookmytree.platform.metaverse.controller;

import com.bookmytree.platform.metaverse.dto.CropRecommendResponseDto;
import com.bookmytree.platform.metaverse.dto.DigitalTwinResponseDto;
import com.bookmytree.platform.metaverse.dto.GreenPointsResponseDto;
import com.bookmytree.platform.metaverse.dto.SoilReportRequestDto;
import com.bookmytree.platform.metaverse.dto.VirtualFarmItemResponseDto;
import com.bookmytree.platform.metaverse.dto.VirtualPlantRequestDto;
import com.bookmytree.platform.metaverse.service.MetaverseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/metaverse")
@RequiredArgsConstructor
public class MetaverseController {

    private final MetaverseService metaverseService;

    @PostMapping("/fields/{fieldId}/soil-report")
    public ResponseEntity<DigitalTwinResponseDto> uploadSoilReport(
            @PathVariable UUID fieldId,
            @Valid @RequestBody SoilReportRequestDto request
    ) {
        // Ensure path variable matches request body fieldId
        request.setFieldId(fieldId);
        DigitalTwinResponseDto response = metaverseService.uploadSoilReport(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fields/{fieldId}/digital-twin")
    public ResponseEntity<DigitalTwinResponseDto> getFieldDigitalTwin(@PathVariable UUID fieldId) {
        DigitalTwinResponseDto response = metaverseService.getFieldDigitalTwin(fieldId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/crops/recommend")
    public ResponseEntity<CropRecommendResponseDto> recommendCrops(@RequestParam UUID fieldId) {
        CropRecommendResponseDto response = metaverseService.recommendCrops(fieldId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/virtual-farm/plant")
    public ResponseEntity<VirtualFarmItemResponseDto> plantVirtualTree(@Valid @RequestBody VirtualPlantRequestDto request) {
        VirtualFarmItemResponseDto response = metaverseService.plantVirtualTree(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/virtual-farm/{userId}")
    public ResponseEntity<List<VirtualFarmItemResponseDto>> getVirtualFarmLayout(@PathVariable UUID userId) {
        List<VirtualFarmItemResponseDto> layout = metaverseService.getVirtualFarmLayout(userId);
        return ResponseEntity.ok(layout);
    }

    @GetMapping("/green-points/balance")
    public ResponseEntity<GreenPointsResponseDto> getGreenPointsBalance(@RequestParam UUID userId) {
        GreenPointsResponseDto response = metaverseService.getGreenPointsBalance(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/green-points/award")
    public ResponseEntity<GreenPointsResponseDto> awardPoints(
            @RequestParam UUID userId,
            @RequestParam int points,
            @RequestParam double carbonOffsetKg
    ) {
        GreenPointsResponseDto response = metaverseService.awardGreenPoints(userId, points, carbonOffsetKg);
        return ResponseEntity.ok(response);
    }
}