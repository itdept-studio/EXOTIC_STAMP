package metro.ExoticStamp.modules.metro.domain.exception;

public class StationInactiveException extends RuntimeException {

    public StationInactiveException(Integer stationId) {
        super("Station is inactive: " + stationId);
    }
}

