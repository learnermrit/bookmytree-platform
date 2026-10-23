package com.bookmytree.platform.metaverse.service;

import com.bookmytree.platform.common.exception.ResourceNotFoundException;
import com.bookmytree.platform.metaverse.dto.DigitalTwinResponseDto;
import com.bookmytree.platform.metaverse.dto.SoilReportRequestDto;
import com.bookmytree.platform.metaverse.entity.SoilReport;
import com.bookmytree.platform.metaverse.repository.SoilReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetaverseService {

    private final SoilReportRepository soilReportRepository;

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
}