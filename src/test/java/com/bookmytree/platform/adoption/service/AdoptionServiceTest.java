package com.bookmytree.platform.adoption.service;

import com.bookmytree.platform.adoption.dto.AdoptionCheckoutRequestDto;
import com.bookmytree.platform.adoption.dto.CreateAdoptionListingRequestDto;
import com.bookmytree.platform.adoption.entity.AdoptionCategory;
import com.bookmytree.platform.adoption.entity.AdoptionListing;
import com.bookmytree.platform.adoption.repository.AdoptionListingRepository;
import com.bookmytree.platform.adoption.repository.UserAdoptionRepository;
import com.bookmytree.platform.auth.repository.UserRepository;
import com.bookmytree.platform.metaverse.service.MetaverseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdoptionServiceTest {

    @Mock
    private AdoptionListingRepository adoptionListingRepository;

    @Mock
    private UserAdoptionRepository userAdoptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MetaverseService metaverseService;

    @InjectMocks
    private AdoptionService adoptionService;

    @Test
    void adoptListing_shouldReduceInventoryAndAwardPoints() {
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();

        AdoptionListing listing = new AdoptionListing();
        listing.setId(listingId);
        listing.setFarmerId(UUID.randomUUID());
        listing.setCategory(AdoptionCategory.PLANT);
        listing.setTitle("Tomato Plants");
        listing.setName("Tomato");
        listing.setQuantityAvailable(10);
        listing.setPrice(BigDecimal.valueOf(180));
        listing.setPointsReward(120);
        listing.setCarbonOffsetKg(BigDecimal.valueOf(1.6));
        listing.setActive(true);

        when(adoptionListingRepository.findById(listingId)).thenReturn(Optional.of(listing));
        when(userAdoptionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(metaverseService.awardGreenPoints(any(), any(Integer.class), any(Double.class))).thenReturn(null);

        AdoptionCheckoutRequestDto request = new AdoptionCheckoutRequestDto();
        request.setUserId(userId);
        request.setListingId(listingId);
        request.setQuantity(3);

        var response = adoptionService.adoptListing(request);

        assertEquals(7, listing.getQuantityAvailable());
        assertEquals(3, response.getQuantity());
        verify(userAdoptionRepository).save(any());
    }

    @Test
    void createListing_shouldPersistFarmerAdoption() {
        UUID farmerId = UUID.randomUUID();
        CreateAdoptionListingRequestDto request = new CreateAdoptionListingRequestDto();
        request.setFarmerId(farmerId);
        request.setCategory(AdoptionCategory.CROP_FIELD);
        request.setTitle("Rice Field");
        request.setName("Rice");
        request.setQuantityAvailable(8);
        request.setPrice(BigDecimal.valueOf(500));
        request.setPointsReward(300);
        request.setCarbonOffsetKg(BigDecimal.valueOf(5.5));

        when(adoptionListingRepository.save(any(AdoptionListing.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        adoptionService.createListing(request);

        verify(adoptionListingRepository).save(any(AdoptionListing.class));
    }
}
