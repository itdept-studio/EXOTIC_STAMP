package metro.ExoticStamp.modules.collection.domain.exception;

import java.util.UUID;

public class StampAlreadyCollectedException extends RuntimeException {
    public StampAlreadyCollectedException(UUID stationId) {
        super("You have already collected a stamp at this station: " + stationId);
    }
}

