package metro.ExoticStamp.modules.collection.domain.exception;

import java.util.UUID;

public class CampaignNotFoundException extends RuntimeException {
    public CampaignNotFoundException(UUID campaignId) {
        super(campaignId == null ? "Default campaign not found" : "Campaign not found: " + campaignId);
    }
}

