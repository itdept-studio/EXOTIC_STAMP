package metro.ExoticStamp.modules.reward.infrastructure.event;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.collection.domain.event.StampCollectedEvent;
import metro.ExoticStamp.modules.reward.application.port.RewardStampCollectedDedupPort;
import metro.ExoticStamp.modules.reward.application.service.RewardCommandService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StampCollectedEventListener {

    private final RewardStampCollectedDedupPort rewardStampCollectedDedupPort;
    private final RewardCommandService rewardCommandService;
    private final MeterRegistry meterRegistry;

    @Async
    @EventListener
    public void onStampCollected(StampCollectedEvent event) {
        try {
            if (!rewardStampCollectedDedupPort.claimFirstProcessing(event.getEventId())) {
                log.debug("[Reward] duplicate StampCollectedEvent skipped eventId={} userId={}",
                        event.getEventId(), event.getUserId());
                return;
            }
            UUID lineId = event.getLineId();
            String lineTag = lineId != null ? lineId.toString() : "unknown";
            meterRegistry.counter("reward.milestone.checked", "lineId", lineTag).increment();
            rewardCommandService.handleStampCollected(event.getUserId(), lineId, event.getCampaignId());
        } catch (Exception e) {
            log.error("[Reward] StampCollectedEvent handling failed eventId={} userId={}: {}",
                    event.getEventId(), event.getUserId(), e.getMessage(), e);
        }
    }
}
