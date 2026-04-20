package metro.ExoticStamp.modules.metro.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStationRequest {

    @Size(max = 20)
    private String code;

    @Size(max = 100)
    private String name;

    @Min(1)
    private Integer sequence;

    @Size(max = 500)
    private String description;

    private String historicalInfo;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @Size(max = 100)
    private String nfcTagId;

    @Size(max = 100)
    private String qrCodeToken;

    private Boolean isActive;
}
