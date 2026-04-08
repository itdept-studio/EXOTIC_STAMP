package metro.ExoticStamp.modules.collection.infrastructure.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.metro.application.LineQueryService;
import metro.ExoticStamp.modules.metro.presentation.dto.response.LineResponse;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectionBootstrapper implements ApplicationRunner {

    private final LineQueryService lineQueryService;
    private final CampaignRepository campaignRepository;

    @Override
    public void run(ApplicationArguments args) {
        List<LineResponse> activeLines = lineQueryService.getAllLines(true);
        for (LineResponse line : activeLines) {
            UUID lineId = line.getId();
            if (lineId == null) continue;
            if (campaignRepository.existsDefaultByLineId(lineId)) continue;

            LocalDateTime now = LocalDateTime.now();
            Campaign campaign = Campaign.builder()
                    .lineId(lineId)
                    .isDefault(true)
                    .isActive(true)
                    .code(defaultCampaignCode(lineId))
                    .name("Default campaign: " + line.getName())
                    .description("Auto-created default campaign for line " + line.getCode())
                    .startDate(now)
                    .endDate(now.plusYears(50))
                    .createdAt(now)
                    .build();
            campaignRepository.save(campaign);
            log.info("[Collection] Bootstrapped default campaign for lineId={}", lineId);
        }
    }

    private String defaultCampaignCode(UUID lineId) {
        String compact = lineId.toString().replace("-", "");
        return ("DEF-" + compact).substring(0, 30);
    }
}

