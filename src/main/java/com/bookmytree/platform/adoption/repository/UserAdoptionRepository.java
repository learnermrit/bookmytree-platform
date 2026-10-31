package com.bookmytree.platform.adoption.repository;

import com.bookmytree.platform.adoption.entity.UserAdoptionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserAdoptionRepository extends JpaRepository<UserAdoptionRecord, UUID> {
    List<UserAdoptionRecord> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<UserAdoptionRecord> findByListingIdOrderByCreatedAtDesc(UUID listingId);
}
