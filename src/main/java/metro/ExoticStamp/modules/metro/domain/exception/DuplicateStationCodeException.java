package metro.ExoticStamp.modules.metro.domain.exception;

public class DuplicateStationCodeException extends RuntimeException {

    public DuplicateStationCodeException(String code, Integer lineId) {
        super("Station code already exists on this line: " + code + " (lineId=" + lineId + ")");
    }
}
