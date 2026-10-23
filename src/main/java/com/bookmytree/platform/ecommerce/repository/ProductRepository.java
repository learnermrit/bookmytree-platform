package com.bookmytree.platform.ecommerce.repository;

import com.bookmytree.platform.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByCategory(Product.ProductCategory category);
    List<Product> findByFarmerId(UUID farmerId);
}