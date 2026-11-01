package com.bookmytree.platform.adoption.service;

import com.bookmytree.platform.adoption.dto.AdoptionCheckoutRequestDto;
import com.bookmytree.platform.adoption.dto.AdoptionListingResponseDto;
import com.bookmytree.platform.adoption.dto.CreateAdoptionListingRequestDto;
import com.bookmytree.platform.adoption.dto.UserAdoptionRecordResponseDto;
import com.bookmytree.platform.adoption.entity.AdoptionListing;
import com.bookmytree.platform.adoption.entity.AdoptionStatus;
import com.bookmytree.platform.adoption.entity.UserAdoptionRecord;
import com.bookmytree.platform.auth.entity.User;
import com.bookmytree.platform.auth.repository.UserRepository;
import com.bookmytree.platform.adoption.repository.AdoptionListingRepository;
import com.bookmytree.platform.adoption.repository.UserAdoptionRepository;
import com.bookmytree.platform.common.exception.BadRequestException;
import com.bookmytree.platform.common.exception.ResourceNotFoundException;
import com.bookmytree.platform.metaverse.service.MetaverseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdoptionService {

    private final AdoptionListingRepository adoptionListingRepository;
    private final UserAdoptionRepository userAdoptionRepository;
    private final UserRepository userRepository;
    private final MetaverseService metaverseService;

    @Transactional
    public AdoptionListingResponseDto createListing(CreateAdoptionListingRequestDto request) {
        AdoptionListing listing = new AdoptionListing();
        listing.setFarmerId(request.getFarmerId());
        listing.setCategory(request.getCategory());
        listing.setTitle(request.getTitle());
        listing.setName(request.getName());
        listing.setQuantityAvailable(request.getQuantityAvailable());
        listing.setPrice(request.getPrice());
        listing.setPointsReward(request.getPointsReward());
        listing.setCarbonOffsetKg(request.getCarbonOffsetKg());
        listing.setDescription(request.getDescription());
        listing.setLocation(request.getLocation());
        listing.setImageUrl(request.getImageUrl());
        listing.setUnitLabel(request.getUnitLabel());
        listing.setActive(true);

        AdoptionListing saved = adoptionListingRepository.save(listing);
        log.info("Farmer {} created adoption listing {} with {} units", request.getFarmerId(), saved.getId(),
                saved.getQuantityAvailable());
        return mapToListingResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AdoptionListingResponseDto> getActiveListings() {
        return adoptionListingRepository.findByActiveTrueOrderByCreatedAtDesc().stream()
                .map(this::mapToListingResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdoptionListingResponseDto> getFarmerListings(UUID farmerId) {
        return adoptionListingRepository.findByFarmerIdOrderByCreatedAtDesc(farmerId).stream()
                .map(this::mapToListingResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserAdoptionRecordResponseDto> getUserAdoptions(UUID userId) {
        return userAdoptionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(record -> {
                    AdoptionListing listing = adoptionListingRepository.findById(record.getListingId())
                            .orElse(null);
                    return mapToUserAdoptionResponse(record, listing);
                })
                .toList();
    }

    @Transactional
    public AdoptionListingResponseDto updateListingAvailability(UUID listingId, boolean active) {
        AdoptionListing listing = adoptionListingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Adoption listing not found."));
        listing.setActive(active);
        return mapToListingResponse(adoptionListingRepository.save(listing));
    }

    @Transactional
    public UserAdoptionRecordResponseDto adoptListing(AdoptionCheckoutRequestDto request) {
        if (request.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must be at least 1.");
        }

        AdoptionListing listing = adoptionListingRepository.findById(request.getListingId())
                .orElseThrow(() -> new ResourceNotFoundException("Adoption listing not found."));

        if (!listing.isActive()) {
            throw new BadRequestException("This adoption listing is no longer active.");
        }

        if (listing.getQuantityAvailable() < request.getQuantity()) {
            throw new BadRequestException("Only " + listing.getQuantityAvailable() + " units are currently available.");
        }

        int updatedQuantity = listing.getQuantityAvailable() - request.getQuantity();
        listing.setQuantityAvailable(updatedQuantity);
        if (updatedQuantity <= 0) {
            listing.setActive(false);
        }
        adoptionListingRepository.save(listing);

        BigDecimal amountPaid = listing.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        Integer pointsAwarded = listing.getPointsReward() * request.getQuantity();
        BigDecimal carbonOffsetKg = listing.getCarbonOffsetKg().multiply(BigDecimal.valueOf(request.getQuantity()));

        UserAdoptionRecord record = new UserAdoptionRecord();
        record.setUserId(request.getUserId());
        record.setListingId(request.getListingId());
        record.setQuantity(request.getQuantity());
        record.setAmountPaid(amountPaid);
        record.setPointsAwarded(pointsAwarded);
        record.setCarbonOffsetKg(carbonOffsetKg);
        record.setStatus(AdoptionStatus.ADOPTED);
        UserAdoptionRecord saved = userAdoptionRepository.save(record);

        metaverseService.awardGreenPoints(request.getUserId(), pointsAwarded, carbonOffsetKg.doubleValue());

        log.info("User {} adopted {} units of listing {} and earned {} points", request.getUserId(),
                request.getQuantity(), listing.getId(), pointsAwarded);
        return mapToUserAdoptionResponse(saved, listing);
    }

    private AdoptionListingResponseDto mapToListingResponse(AdoptionListing listing) {
        User farmer = userRepository.findById(listing.getFarmerId()).orElse(null);

        return AdoptionListingResponseDto.builder()
                .id(listing.getId())
                .farmerId(listing.getFarmerId())
                .farmerName(farmer != null ? farmer.getFullName() : "Farmer")
                .category(listing.getCategory())
                .title(listing.getTitle())
                .name(listing.getName())
                .quantityAvailable(listing.getQuantityAvailable())
                .price(listing.getPrice())
                .pointsReward(listing.getPointsReward())
                .carbonOffsetKg(listing.getCarbonOffsetKg())
                .description(listing.getDescription())
                .location(listing.getLocation())
                .imageUrl(listing.getImageUrl())
                .unitLabel(listing.getUnitLabel())
                .active(listing.isActive())
                .createdAt(listing.getCreatedAt())
                .build();
    }

    private UserAdoptionRecordResponseDto mapToUserAdoptionResponse(UserAdoptionRecord record,
            AdoptionListing listing) {
        String listingTitle = listing != null ? listing.getTitle() : "Adopted listing";
        String listingName = listing != null ? listing.getName() : "Unknown";

        return UserAdoptionRecordResponseDto.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .listingId(record.getListingId())
                .listingTitle(listingTitle)
                .listingName(listingName)
                .quantity(record.getQuantity())
                .amountPaid(record.getAmountPaid())
                .pointsAwarded(record.getPointsAwarded())
                .carbonOffsetKg(record.getCarbonOffsetKg())
                .status(record.getStatus())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
