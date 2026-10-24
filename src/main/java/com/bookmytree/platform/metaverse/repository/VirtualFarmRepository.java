package com.bookmytree.platform.metaverse.repository;

import com.bookmytree.platform.metaverse.entity.VirtualFarmItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VirtualFarmRepository extends JpaRepository<VirtualFarmItem, UUID> {
    List<VirtualFarmItem> findByUserId(UUID userId);
}