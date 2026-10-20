package com.bookmytree.platform.inventory.repository;

import com.bookmytree.platform.inventory.entity.TreeBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TreeBranchRepository extends JpaRepository<TreeBranch, UUID> {
}