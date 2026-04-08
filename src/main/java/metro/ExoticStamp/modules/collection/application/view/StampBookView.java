package metro.ExoticStamp.modules.collection.application.view;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record StampBookView(
        UUID lineId,
        UUID campaignId,
        List<StationCellView> stations
) {
    @Builder
    public record StationCellView(
            UUID stationId,
            String stationName,
            Integer sequence,
            boolean collected,
            String stampDesignUrl
    ) {
    }
}
