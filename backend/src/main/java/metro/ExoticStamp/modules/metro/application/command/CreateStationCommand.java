package metro.ExoticStamp.modules.metro.application.command;

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
public class CreateStationCommand {
    private String code;
    private String name;
    private UUID lineId;
    private Integer sequence;
    private String description;
    private String historicalInfo;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String nfcTagId;
    private String qrCodeToken;
    private Boolean isActive;
}
