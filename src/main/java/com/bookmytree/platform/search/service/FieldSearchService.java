package com.bookmytree.platform.search.service;

import com.bookmytree.platform.search.entity.Field;
import com.bookmytree.platform.search.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FieldSearchService {

    private final FieldRepository fieldRepository;

    @Transactional(readOnly = true)
    public List<Field> searchNearbyOrchards(double latitude, double longitude, double radiusKm, String species,
            Double minRating) {
        double radiusInMeters = radiusKm * 1000.0;
        log.info("Executing spatial proximity search for lat: {}, lng: {}, radius: {}m, species: {}, minRating: {}",
                latitude, longitude, radiusInMeters, species, minRating);

        List<Field> results = fieldRepository.findNearbyFields(longitude, latitude, radiusInMeters, species, minRating);

        log.info("Spatial query completed successfully. Found {} matching fields.", results.size());
        return results;
    }
}