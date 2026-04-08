package metro.ExoticStamp.modules.metro.domain.exception;

public class LineNotFoundException extends RuntimeException {

    public LineNotFoundException(Integer lineId) {
        super("Line not found with id: " + lineId);
    }
}

