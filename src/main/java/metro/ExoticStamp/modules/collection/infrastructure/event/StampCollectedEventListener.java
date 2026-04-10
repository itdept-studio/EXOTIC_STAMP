package metro.ExoticStamp.modules.collection.infrastructure.event;

import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.collection.domain.event.StampCollectedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Async hook for downstream processing (analytics, notifications). Failures here do not affect the collect transaction.
 */
@Slf4j
@Component
public class StampCollectedEventListener {

    @Async
    @EventListener
    public void onStampCollected(StampCollectedEvent event) {
        log.debug("[Collection] StampCollectedEvent received eventId={} userId={} stationId={}",
                event.getEventId(), event.getUserId(), event.getStationId());
    }
}
