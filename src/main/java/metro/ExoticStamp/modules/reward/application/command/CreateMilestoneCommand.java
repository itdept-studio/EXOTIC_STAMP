package metro.ExoticStamp.modules.reward.application.command;

import java.util.UUID;

public record CreateMilestoneCommand(
        UUID lineId,
        UUID campaignId,
        int stampsRequired,
        String name,
        String description
) {
}
