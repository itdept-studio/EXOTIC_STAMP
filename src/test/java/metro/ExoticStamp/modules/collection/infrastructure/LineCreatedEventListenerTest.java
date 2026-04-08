package metro.ExoticStamp.modules.collection.infrastructure;

import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.infrastructure.event.LineCreatedEventListener;
import metro.ExoticStamp.modules.metro.application.LineQueryService;
import metro.ExoticStamp.modules.metro.domain.event.LineCreatedEvent;
import metro.ExoticStamp.modules.metro.presentation.dto.response.LineDetailResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LineCreatedEventListenerTest {

    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Mock private CampaignRepository campaignRepository;
    @Mock private LineQueryService lineQueryService;

    private LineCreatedEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new LineCreatedEventListener(campaignRepository, lineQueryService);
    }

    @Test
    void createsDefaultCampaign_whenMissing() {
        when(campaignRepository.existsDefaultByLineId(LINE_ID)).thenReturn(false);
        when(lineQueryService.getLineDetail(LINE_ID, true))
                .thenReturn(LineDetailResponse.builder().id(LINE_ID).code("L1").name("Line 1").build());

        listener.onLineCreated(new LineCreatedEvent(LINE_ID));

        verify(campaignRepository).save(any());
    }

    @Test
    void idempotent_skip_whenAlreadyExists() {
        when(campaignRepository.existsDefaultByLineId(LINE_ID)).thenReturn(true);

        listener.onLineCreated(new LineCreatedEvent(LINE_ID));

        verify(campaignRepository, never()).save(any());
        verify(lineQueryService, never()).getLineDetail(any(), anyBoolean());
    }
}

