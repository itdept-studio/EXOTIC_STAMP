package metro.ExoticStamp.modules.collection.presentation.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStampResponse {
    private UUID stampId;
    private UUID stationId;
    private UUID lineId;
    private UUID campaignId;
    private String stationName;
    private String stampDesignUrl;
    private LocalDateTime collectedAt;
    private String collectMethod;
}

