package metro.ExoticStamp.modules.reward.presentation.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateMilestoneRequest {

    private UUID lineId;

    private UUID campaignId;

    @NotNull
    @Min(1)
    private Integer stampsRequired;

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;
}
