package metro.ExoticStamp.modules.reward.application.view;

import lombok.Builder;

import java.util.UUID;

@Builder
public record MilestoneView(
        UUID id,
        UUID lineId,
        UUID campaignId,
        int stampsRequired,
        String name,
        String description,
        boolean active
) {
}
