package metro.ExoticStamp.modules.collection.application.mapper;

import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import org.springframework.stereotype.Component;

@Component
public class CampaignAppMapper {
    public Campaign copyForReturn(Campaign campaign) {
        return campaign;
    }
}

