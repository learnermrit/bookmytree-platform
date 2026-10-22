package com.bookmytree.platform.search.service;

import com.bookmytree.platform.search.dto.FieldResponseDto;
import com.bookmytree.platform.search.entity.Field;
import com.bookmytree.platform.search.repository.FieldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FieldSearchService {

    private final FieldRepository fieldRepository;

    @Transactional(readOnly = true)
    public List<FieldResponseDto> searchNearbyOrchards(double latitude, double longitude, double radiusKm, String species, Double minRating) {
        double radiusInMeters = radiusKm * 1000.0;
        log.info("Executing spatial proximity search for lat: {}, lng: {}, radius: {}m", latitude, longitude, radiusInMeters);

        List<Field> fields = fieldRepository.findNearbyFields(longitude, latitude, radiusInMeters, species, minRating);
        
        return fields.stream().map(field -> FieldResponseDto.builder()
                .id(field.getId())
                .name(field.getName())
                .farmerId(field.getFarmerId())
                .latitude(field.getLocation() != null ? field.getLocation().getY() : null)
                .longitude(field.getLocation() != null ? field.getLocation().getX() : null)
                .rating(field.getRating())
                .totalTreesCount(field.getTotalTreesCount())
                .build()
        ).collect(Collectors.toList());
    }
}