package metro.ExoticStamp.modules.collection.presentation.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StampBookResponse {
    private UUID lineId;
    private UUID campaignId;
    private List<StampBookStationResponse> stations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StampBookStationResponse {
        private UUID stationId;
        private String stationName;
        private Integer sequence;
        private boolean collected;
        private String stampDesignUrl;
    }
}

