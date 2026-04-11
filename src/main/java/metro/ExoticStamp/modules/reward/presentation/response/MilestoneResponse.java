package metro.ExoticStamp.modules.reward.presentation.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record MilestoneResponse(
        UUID id,
        UUID lineId,
        UUID campaignId,
        int stampsRequired,
        String name,
        String description,
        boolean active
) {
}
