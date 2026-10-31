package com.bookmytree.platform.adoption.controller;

import com.bookmytree.platform.adoption.dto.AdoptionCheckoutRequestDto;
import com.bookmytree.platform.adoption.dto.AdoptionListingResponseDto;
import com.bookmytree.platform.adoption.dto.CreateAdoptionListingRequestDto;
import com.bookmytree.platform.adoption.dto.UserAdoptionRecordResponseDto;
import com.bookmytree.platform.adoption.service.AdoptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/adoption")
@RequiredArgsConstructor
public class AdoptionController {

    private final AdoptionService adoptionService;

    @GetMapping("/listings")
    public ResponseEntity<List<AdoptionListingResponseDto>> getActiveListings() {
        return ResponseEntity.ok(adoptionService.getActiveListings());
    }

    @GetMapping("/farmer/{farmerId}/listings")
    public ResponseEntity<List<AdoptionListingResponseDto>> getFarmerListings(@PathVariable UUID farmerId) {
        return ResponseEntity.ok(adoptionService.getFarmerListings(farmerId));
    }

    @PostMapping("/listings")
    public ResponseEntity<AdoptionListingResponseDto> createListing(
            @Valid @RequestBody CreateAdoptionListingRequestDto request) {
        return new ResponseEntity<>(adoptionService.createListing(request), HttpStatus.CREATED);
    }

    @PostMapping("/adopt")
    public ResponseEntity<UserAdoptionRecordResponseDto> adoptListing(
            @Valid @RequestBody AdoptionCheckoutRequestDto request) {
        return ResponseEntity.ok(adoptionService.adoptListing(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserAdoptionRecordResponseDto>> getUserAdoptions(@PathVariable UUID userId) {
        return ResponseEntity.ok(adoptionService.getUserAdoptions(userId));
    }
}
