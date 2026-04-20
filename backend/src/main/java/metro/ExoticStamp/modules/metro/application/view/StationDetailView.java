package metro.ExoticStamp.modules.metro.application.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationDetailView {
    private UUID id;
    private UUID lineId;
    private String code;
    private String name;
    private Integer sequence;
    private String description;
    private String historicalInfo;
    private String imageUrl;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String nfcTagId;
    private String qrCodeToken;
    private Integer collectorCount;
    private boolean isActive;
}
