package com.bookmytree.platform.adoption.repository;

import com.bookmytree.platform.adoption.entity.AdoptionListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdoptionListingRepository extends JpaRepository<AdoptionListing, UUID> {
    List<AdoptionListing> findByActiveTrueOrderByCreatedAtDesc();

    List<AdoptionListing> findByFarmerIdOrderByCreatedAtDesc(UUID farmerId);
}
