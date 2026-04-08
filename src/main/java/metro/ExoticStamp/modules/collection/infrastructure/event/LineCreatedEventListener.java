package metro.ExoticStamp.modules.collection.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.metro.application.LineQueryService;
import metro.ExoticStamp.modules.metro.domain.event.LineCreatedEvent;
import metro.ExoticStamp.modules.metro.presentation.dto.response.LineDetailResponse;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LineCreatedEventListener {

    private final CampaignRepository campaignRepository;
    private final LineQueryService lineQueryService;

    @Async
    @EventListener
    public void onLineCreated(LineCreatedEvent event) {
        UUID lineId = event.lineId();
        if (lineId == null) return;

        if (campaignRepository.existsDefaultByLineId(lineId)) {
            return;
        }

        LineDetailResponse line = lineQueryService.getLineDetail(lineId, true);
        Campaign campaign = Campaign.builder()
                .lineId(lineId)
                .isDefault(true)
                .isActive(true)
                .code(defaultCampaignCode(lineId))
                .name("Default campaign: " + line.getName())
                .description("Auto-created default campaign for line " + line.getCode())
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusYears(50))
                .createdAt(LocalDateTime.now())
                .build();

        campaignRepository.save(campaign);
        log.info("[Collection] Default campaign created for lineId={}", lineId);
    }

    private String defaultCampaignCode(UUID lineId) {
        String compact = lineId.toString().replace("-", "");
        return ("DEF-" + compact).substring(0, 30);
    }
}

