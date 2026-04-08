package metro.ExoticStamp.modules.collection.domain.exception;

import java.util.UUID;

public class InvalidStationException extends RuntimeException {
    public InvalidStationException(UUID stationId, String reason) {
        super("Invalid station " + stationId + ": " + reason);
    }
}

