package metro.ExoticStamp.modules.metro.domain.exception;

public class StationNotFoundException extends RuntimeException {

    public StationNotFoundException(String field, String value) {
        super("Station not found with " + field + ": " + value);
    }

    public StationNotFoundException(Integer stationId) {
        super("Station not found with id: " + stationId);
    }
}

