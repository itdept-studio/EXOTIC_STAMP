package metro.ExoticStamp.modules.metro.domain.exception;

import java.util.UUID;

public class StationInactiveException extends RuntimeException {

    public StationInactiveException(UUID stationId) {
        super("Station is inactive: " + stationId);
    }
}




