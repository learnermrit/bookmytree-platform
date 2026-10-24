package com.bookmytree.platform.metaverse.repository;

import com.bookmytree.platform.metaverse.entity.GreenPointsLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GreenPointsRepository extends JpaRepository<GreenPointsLedger, UUID> {
    Optional<GreenPointsLedger> findByUserId(UUID userId);
}