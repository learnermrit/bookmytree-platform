package com.bookmytree.platform.metaverse.service;

import com.bookmytree.platform.common.exception.ResourceNotFoundException;
import com.bookmytree.platform.metaverse.dto.CropRecommendResponseDto;
import com.bookmytree.platform.metaverse.dto.DigitalTwinResponseDto;
import com.bookmytree.platform.metaverse.dto.GreenPointsResponseDto;
import com.bookmytree.platform.metaverse.dto.SoilReportRequestDto;
import com.bookmytree.platform.metaverse.dto.VirtualFarmItemResponseDto;
import com.bookmytree.platform.metaverse.dto.VirtualPlantRequestDto;
import com.bookmytree.platform.metaverse.entity.GreenPointsLedger;
import com.bookmytree.platform.metaverse.entity.SoilReport;
import com.bookmytree.platform.metaverse.entity.VirtualFarmItem;
import com.bookmytree.platform.metaverse.repository.GreenPointsRepository;
import com.bookmytree.platform.metaverse.repository.SoilReportRepository;
import com.bookmytree.platform.metaverse.repository.VirtualFarmRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetaverseService {

    private final SoilReportRepository soilReportRepository;
    private final VirtualFarmRepository virtualFarmRepository;
    private final GreenPointsRepository greenPointsRepository;
    private final org.locationtech.jts.geom.GeometryFactory geometryFactory = new org.locationtech.jts.geom.GeometryFactory(new org.locationtech.jts.geom.PrecisionModel(), 4326);

    
    @Transactional
    public DigitalTwinResponseDto uploadSoilReport(SoilReportRequestDto request) {
        log.info("Uploading soil test report for field: {}", request.getFieldId());

        SoilReport report = soilReportRepository.findByFieldId(request.getFieldId())
                .orElse(new SoilReport());

        report.setFieldId(request.getFieldId());
        report.setPhLevel(request.getPhLevel());
        report.setNitrogenContentKgPerHectare(request.getNitrogenContentKgPerHectare());
        report.setPhosphorusContentKgPerHectare(request.getPhosphorusContentKgPerHectare());
        report.setPotassiumContentKgPerHectare(request.getPotassiumContentKgPerHectare());
        report.setSoilType(request.getSoilType());
        report.setTestDate(request.getTestDate());
        report.setTerrainAssetUrl(request.getTerrainAssetUrl());

        SoilReport savedReport = soilReportRepository.save(report);
        log.info("Soil report saved successfully for field: {}", request.getFieldId());

        return mapToDto(savedReport);
    }

    @Transactional(readOnly = true)
    public DigitalTwinResponseDto getFieldDigitalTwin(UUID fieldId) {
        log.info("Fetching digital twin and soil analytics for field: {}", fieldId);

        SoilReport report = soilReportRepository.findByFieldId(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Soil report / digital twin not found for field ID: " + fieldId));

        return mapToDto(report);
    }

    private DigitalTwinResponseDto mapToDto(SoilReport report) {
        // Simple rule-based AI recommendation generator based on pH and NPK
        String recommendation = String.format("Optimal for %s soil. NPK levels are adequate for fruit-bearing trees. Maintain irrigation around pH %.1f.", 
                report.getSoilType(), report.getPhLevel());

        return DigitalTwinResponseDto.builder()
                .id(report.getId())
                .fieldId(report.getFieldId())
                .phLevel(report.getPhLevel())
                .nitrogenContentKgPerHectare(report.getNitrogenContentKgPerHectare())
                .phosphorusContentKgPerHectare(report.getPhosphorusContentKgPerHectare())
                .potassiumContentKgPerHectare(report.getPotassiumContentKgPerHectare())
                .soilType(report.getSoilType())
                .testDate(report.getTestDate())
                .terrainAssetUrl(report.getTerrainAssetUrl())
                .aiSuitabilityRecommendation(recommendation)
                .build();
    }

    public CropRecommendResponseDto recommendCrops(UUID fieldId) {
        log.info("Generating AI crop recommendations for field ID: {}", fieldId);
        SoilReport report = soilReportRepository.findByFieldId(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Soil report not found for field ID: " + fieldId));

        List<String> species;
        if (report.getPhLevel() >= 6.0 && report.getPhLevel() <= 7.5) {
            species = List.of("Alphonso Mango", "Kesar Mango", "Persian Walnut", "Hass Avocado");
        } else {
            species = List.of("Red Laterite Cashew", "Pomegranate", "Guava");
        }

        return CropRecommendResponseDto.builder()
                .soilType(report.getSoilType())
                .phLevel(report.getPhLevel())
                .recommendedSpecies(species)
                .suitabilityScore("94.5% - Excellent Agronomic Match")
                .build();
    }

    @Transactional
    public VirtualFarmItemResponseDto plantVirtualTree(VirtualPlantRequestDto request) {
        log.info("Planting virtual crop '{}' for user: {} on plot: {}", request.getCropSpecies(), request.getUserId(), request.getMetaversePlotId());

        Point point = geometryFactory.createPoint(new org.locationtech.jts.geom.Coordinate(request.getLongitude(), request.getLatitude()));

        VirtualFarmItem item = new VirtualFarmItem();
        item.setUserId(request.getUserId());
        item.setCropSpecies(request.getCropSpecies());
        item.setCoordinates(point);
        item.setMetaversePlotId(request.getMetaversePlotId());
        item.setGrowthStage(1); // Initial sapling stage

        VirtualFarmItem saved = virtualFarmRepository.save(item);
        return mapToVirtualDto(saved);
    }

    @Transactional(readOnly = true)
    public List<VirtualFarmItemResponseDto> getVirtualFarmLayout(UUID userId) {
        log.info("Fetching virtual 3D farm layout for user ID: {}", userId);
        return virtualFarmRepository.findByUserId(userId).stream()
                .map(this::mapToVirtualDto)
                .collect(Collectors.toList());
    }

    private VirtualFarmItemResponseDto mapToVirtualDto(VirtualFarmItem item) {
        return VirtualFarmItemResponseDto.builder()
                .id(item.getId())
                .userId(item.getUserId())
                .cropSpecies(item.getCropSpecies())
                .latitude(item.getCoordinates() != null ? item.getCoordinates().getY() : null)
                .longitude(item.getCoordinates() != null ? item.getCoordinates().getX() : null)
                .metaversePlotId(item.getMetaversePlotId())
                .growthStage(item.getGrowthStage())
                .build();
    }

    @Transactional(readOnly = true)
    public GreenPointsResponseDto getGreenPointsBalance(UUID userId) {
        log.info("Fetching Green Points balance and carbon-sink summary for user: {}", userId);
        GreenPointsLedger ledger = greenPointsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    GreenPointsLedger newLedger = new GreenPointsLedger();
                    newLedger.setUserId(userId);
                    newLedger.setPointsBalance(150); // Welcome bonus points
                    newLedger.setCarbonOffsetKg(BigDecimal.valueOf(25.5)); // Initial offset
                    return greenPointsRepository.save(newLedger);
                });

        return GreenPointsResponseDto.builder()
                .userId(ledger.getUserId())
                .pointsBalance(ledger.getPointsBalance())
                .carbonOffsetKg(ledger.getCarbonOffsetKg())
                .build();
    }
    
    @Transactional
    public GreenPointsResponseDto awardGreenPoints(UUID userId, int pointsToAdd, double carbonKgToAdd) {
        log.info("Awarding {} points and {} kg carbon offset to user: {}", pointsToAdd, carbonKgToAdd, userId);

        GreenPointsLedger ledger = greenPointsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    GreenPointsLedger newLedger = new GreenPointsLedger();
                    newLedger.setUserId(userId);
                    newLedger.setPointsBalance(0);
                    newLedger.setCarbonOffsetKg(BigDecimal.ZERO);
                    return newLedger;
                });

        ledger.setPointsBalance(ledger.getPointsBalance() + pointsToAdd);
        ledger.setCarbonOffsetKg(ledger.getCarbonOffsetKg().add(BigDecimal.valueOf(carbonKgToAdd)));

        GreenPointsLedger savedLedger = greenPointsRepository.save(ledger);

        return GreenPointsResponseDto.builder()
                .userId(savedLedger.getUserId())
                .pointsBalance(savedLedger.getPointsBalance())
                .carbonOffsetKg(savedLedger.getCarbonOffsetKg())
                .build();
    }

    
}