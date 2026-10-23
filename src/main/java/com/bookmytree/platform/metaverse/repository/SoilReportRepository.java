package com.bookmytree.platform.metaverse.repository;

import com.bookmytree.platform.metaverse.entity.SoilReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SoilReportRepository extends JpaRepository<SoilReport, UUID> {
    Optional<SoilReport> findByFieldId(UUID fieldId);
}