package metro.ExoticStamp.modules.metro.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationResponse {

    private Integer id;
    private Integer lineId;
    private String code;
    private String name;
    private Integer sequence;
    private String description;
    private String historicalInfo;
    private String imageUrl;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer collectorCount;
    private boolean isActive;
}

