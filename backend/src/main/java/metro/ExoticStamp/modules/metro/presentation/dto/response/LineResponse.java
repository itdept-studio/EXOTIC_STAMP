package metro.ExoticStamp.modules.metro.presentation.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineResponse {

    private UUID id;
    private String code;
    private String name;
    private String color;
    private Integer totalStations;
    private boolean isActive;
}




