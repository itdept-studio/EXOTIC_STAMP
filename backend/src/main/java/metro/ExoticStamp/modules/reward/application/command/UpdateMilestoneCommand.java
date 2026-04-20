package metro.ExoticStamp.modules.reward.application.command;

import java.util.UUID;

public record UpdateMilestoneCommand(
        UUID id,
        UUID lineId,
        UUID campaignId,
        Integer stampsRequired,
        String name,
        String description
) {
}
