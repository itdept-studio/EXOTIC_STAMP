package metro.ExoticStamp.modules.collection.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.domain.exception.InvalidRequestException;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignStationRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CampaignStationRepositoryAdapter implements CampaignStationRepository {

    private final JpaCampaignStationRepository jpaCampaignStationRepository;

    @Override
    public void assign(UUID campaignId, UUID stationId) {
        if (jpaCampaignStationRepository.existsByCampaignIdAndStationId(campaignId, stationId)) {
            throw new InvalidRequestException("Station already assigned to this campaign");
        }
        jpaCampaignStationRepository.save(CampaignStationEntity.builder()
                .campaignId(campaignId)
                .stationId(stationId)
                .build());
    }

    @Override
    public void remove(UUID campaignId, UUID stationId) {
        jpaCampaignStationRepository.deleteByCampaignIdAndStationId(campaignId, stationId);
    }

    @Override
    public boolean exists(UUID campaignId, UUID stationId) {
        return jpaCampaignStationRepository.existsByCampaignIdAndStationId(campaignId, stationId);
    }
}
