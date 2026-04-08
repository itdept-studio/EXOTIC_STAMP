package metro.ExoticStamp.modules.collection.domain.event;

import metro.ExoticStamp.modules.collection.domain.model.CollectMethod;

import java.time.LocalDateTime;
import java.util.UUID;

public record StampCollectedEvent(
        UUID eventId,
        UUID userId,
        UUID stationId,
        UUID lineId,
        UUID campaignId,
        LocalDateTime collectedAt,
        CollectMethod collectMethod
) {}

