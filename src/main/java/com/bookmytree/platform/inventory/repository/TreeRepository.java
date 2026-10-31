package com.bookmytree.platform.inventory.repository;

import com.bookmytree.platform.inventory.entity.Tree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TreeRepository extends JpaRepository<Tree, UUID> {
    List<Tree> findByFieldId(UUID fieldId);
}
