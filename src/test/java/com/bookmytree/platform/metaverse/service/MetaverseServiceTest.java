package com.bookmytree.platform.metaverse.service;

import com.bookmytree.platform.metaverse.dto.VirtualPlantRequestDto;
import com.bookmytree.platform.metaverse.entity.VirtualFarmItem;
import com.bookmytree.platform.metaverse.repository.GreenPointsRepository;
import com.bookmytree.platform.metaverse.repository.SoilReportRepository;
import com.bookmytree.platform.metaverse.repository.VirtualFarmRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetaverseServiceTest {

    @Mock
    private SoilReportRepository soilReportRepository;

    @Mock
    private VirtualFarmRepository virtualFarmRepository;

    @Mock
    private GreenPointsRepository greenPointsRepository;

    @InjectMocks
    private MetaverseService metaverseService;

    @Test
    void saveVirtualFarmLayout_shouldReplaceUserLayoutAndPersistEntries() {
        UUID userId = UUID.randomUUID();

        VirtualPlantRequestDto tree = new VirtualPlantRequestDto();
        tree.setUserId(userId);
        tree.setCropSpecies("Mango");
        tree.setLatitude(19.0760);
        tree.setLongitude(72.8777);
        tree.setMetaversePlotId("plot-1");

        VirtualPlantRequestDto pond = new VirtualPlantRequestDto();
        pond.setUserId(userId);
        pond.setCropSpecies("Pond");
        pond.setLatitude(19.0765);
        pond.setLongitude(72.8785);
        pond.setMetaversePlotId("plot-2");

        when(virtualFarmRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        metaverseService.saveVirtualFarmLayout(userId, List.of(tree, pond));

        verify(virtualFarmRepository).deleteByUserId(userId);
        verify(virtualFarmRepository).saveAll(any());
    }
}
