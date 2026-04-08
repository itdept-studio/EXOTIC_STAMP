package metro.ExoticStamp.modules.metro.domain.exception;

import lombok.Getter;

@Getter
public class DuplicateStationSequenceException extends RuntimeException {

    private final Integer lineId;
    private final Integer sequence;

    public DuplicateStationSequenceException(Integer lineId, Integer sequence) {
        super("Sequence " + sequence + " already exists on line " + lineId);
        this.lineId = lineId;
        this.sequence = sequence;
    }
}
