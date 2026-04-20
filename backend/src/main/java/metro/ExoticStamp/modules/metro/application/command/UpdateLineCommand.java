package metro.ExoticStamp.modules.metro.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLineCommand {
    private UUID lineId;
    private String code;
    private String name;
    private String color;
    private Boolean isActive;
}
