package metro.ExoticStamp.modules.metro.domain.exception;

import java.util.UUID;

public class StationNotFoundException extends RuntimeException {

    public StationNotFoundException(String field, String value) {
        super("Station not found with " + field + ": " + value);
    }

    public StationNotFoundException(UUID stationId) {
        super("Station not found with id: " + stationId);
    }
}




