package com.bookmytree.platform.gatepass.repository;

import com.bookmytree.platform.gatepass.entity.VisitGatePass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GatePassRepository extends JpaRepository<VisitGatePass, UUID> {
    List<VisitGatePass> findByUserId(UUID userId);
    Optional<VisitGatePass> findByPassCode(String passCode);
}