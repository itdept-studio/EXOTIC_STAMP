package metro.ExoticStamp.modules.collection.infrastructure.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.metro.application.port.LineReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroLineView;
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

    private final LineReadPort lineReadPort;
    private final CampaignRepository campaignRepository;

    @Override
    public void run(ApplicationArguments args) {
        List<MetroLineView> activeLines = lineReadPort.getAllActiveLines();
        for (MetroLineView line : activeLines) {
            UUID lineId = line.id();
            if (lineId == null) continue;
            if (campaignRepository.existsDefaultByLineId(lineId)) continue;

            LocalDateTime now = LocalDateTime.now();
            Campaign campaign = Campaign.builder()
                    .lineId(lineId)
                    .isDefault(true)
                    .isActive(true)
                    .code(defaultCampaignCode(lineId))
                    .name("Default campaign: " + line.name())
                    .description("Auto-created default campaign for line " + line.code())
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

