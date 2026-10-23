package com.bookmytree.platform.metaverse.controller;

import com.bookmytree.platform.metaverse.dto.DigitalTwinResponseDto;
import com.bookmytree.platform.metaverse.dto.SoilReportRequestDto;
import com.bookmytree.platform.metaverse.service.MetaverseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}