package metro.ExoticStamp.modules.metro.presentation.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineDetailResponse {

    private UUID id;
    private String code;
    private String name;
    private String color;
    private Integer totalStations;
    private boolean isActive;
    private List<StationResponse> stations;
}



