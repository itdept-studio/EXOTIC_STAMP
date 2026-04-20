package metro.ExoticStamp.modules.collection.infrastructure;

import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.infrastructure.repository.CampaignRepositoryAdapter;
import metro.ExoticStamp.modules.collection.infrastructure.repository.JpaCampaignRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignRepositoryAdapterTest {

    @Mock
    private JpaCampaignRepository jpa;

    @InjectMocks
    private CampaignRepositoryAdapter adapter;

    @Test
    void findDefaultByLineId_delegates() {
        UUID lineId = UUID.randomUUID();
        Campaign c = Campaign.builder().lineId(lineId).isDefault(true).isActive(true).code("X").name("N").build();
        when(jpa.findByLineIdAndIsDefaultTrueAndIsActiveTrue(lineId)).thenReturn(Optional.of(c));

        assertTrue(adapter.findDefaultByLineId(lineId).isPresent());
        verify(jpa).findByLineIdAndIsDefaultTrueAndIsActiveTrue(lineId);
    }

    @Test
    void existsDefaultByLineId_delegates() {
        UUID lineId = UUID.randomUUID();
        when(jpa.existsByLineIdAndIsDefaultTrue(lineId)).thenReturn(true);
        assertTrue(adapter.existsDefaultByLineId(lineId));
    }
}
