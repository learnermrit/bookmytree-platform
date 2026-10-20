package com.bookmytree.platform.search.repository;

import com.bookmytree.platform.search.entity.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FieldRepository extends JpaRepository<Field, UUID> {

    /**
     * Zomato/Swiggy-style Proximity & Species Search using PostGIS.
     * Uses ST_DWithin to find fields within a specific radius (in meters) from
     * given coordinates,
     * with optional filtering by tree species and minimum rating.
     *
     * @param longitude      User's current longitude
     * @param latitude       User's current latitude
     * @param radiusInMeters Search radius in meters (e.g., 10000m for 10km)
     * @param species        Optional tree species filter (can be null)
     * @param minRating      Optional minimum rating filter (can be null)
     * @return List of matching fields/orchards
     */
    @Query(value = "SELECT DISTINCT f.* FROM fields f " +
            "LEFT JOIN trees t ON t.field_id = f.id " +
            "WHERE ST_DWithin(f.location::geography, ST_MakePoint(:longitude, :latitude)::geography, :radiusInMeters) = true "
            +
            "AND (:species IS NULL OR LOWER(t.species) LIKE LOWER(CONCAT('%', :species, '%'))) " +
            "AND (:minRating IS NULL OR f.rating >= :minRating)", nativeQuery = true)
    List<Field> findNearbyFields(
            @Param("longitude") double longitude,
            @Param("latitude") double latitude,
            @Param("radiusInMeters") double radiusInMeters,
            @Param("species") String species,
            @Param("minRating") Double minRating);
}