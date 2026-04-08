package metro.ExoticStamp.modules.collection.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.application.mapper.UserStampAppMapper;
import metro.ExoticStamp.modules.collection.application.port.UserStampCachePort;
import metro.ExoticStamp.modules.collection.application.view.ProgressView;
import metro.ExoticStamp.modules.collection.application.view.StampBookView;
import metro.ExoticStamp.modules.collection.application.view.UserStampView;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignNotFoundException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.UserStampRepository;
import metro.ExoticStamp.modules.metro.application.port.StationReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroStationView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionQueryService {

    private final CampaignRepository campaignRepository;
    private final StampDesignRepository stampDesignRepository;
    private final UserStampRepository userStampRepository;
    private final StationReadPort stationReadPort;
    private final UserStampCachePort cachePort;
    private final UserStampAppMapper userStampAppMapper;

    public List<UserStampView> getMyStamps(UUID userId, UUID lineId, UUID campaignId) {
        Campaign campaign = resolveCampaign(lineId, campaignId);

        UUID effectiveLineId = campaign.getLineId() != null ? campaign.getLineId() : lineId;
        boolean cacheable = campaignId == null;
        if (cacheable) {
            Optional<List<UserStampView>> cached = cachePort.getUserStamps(userId, effectiveLineId);
            if (cached.isPresent()) return cached.get();
        }

        List<UserStamp> stamps = userStampRepository.findByUserIdAndCampaignId(userId, campaign.getId());
        List<UserStampView> res = mapUserStamps(stamps);

        if (cacheable) {
            cachePort.putUserStamps(userId, effectiveLineId, res);
        }
        return res;
    }

    public ProgressView getMyProgress(UUID userId, UUID lineId, UUID campaignId) {
        Campaign campaign = resolveCampaign(lineId, campaignId);
        UUID effectiveLineId = campaign.getLineId() != null ? campaign.getLineId() : lineId;
        boolean cacheable = campaignId == null;
        if (cacheable) {
            Optional<ProgressView> cached = cachePort.getUserProgress(userId, effectiveLineId);
            if (cached.isPresent()) return cached.get();
        }

        ProgressView computed = computeProgress(userId, effectiveLineId, campaign.getId());
        if (cacheable) {
            cachePort.putUserProgress(userId, effectiveLineId, computed);
        }
        return computed;
    }

    public List<UserStampView> getMyHistory(UUID userId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        List<UserStamp> stamps = userStampRepository.findRecentByUserId(userId, safeLimit);
        return mapUserStamps(stamps);
    }

    public StampBookView getStampBook(UUID userId, UUID lineId, UUID campaignId) {
        Campaign campaign = resolveCampaign(lineId, campaignId);

        List<MetroStationView> lineStations = stationReadPort.listActiveStationsByLineId(lineId);
        List<UserStamp> collected = userStampRepository.findByUserIdAndCampaignId(userId, campaign.getId());
        Set<UUID> collectedStationIds = collected.stream().map(UserStamp::getStationId).collect(HashSet::new, Set::add, Set::addAll);

        List<StampBookView.StationCellView> stations = new ArrayList<>();
        for (MetroStationView s : lineStations) {
            boolean isCollected = collectedStationIds.contains(s.id());
            StampDesign design = stampDesignRepository.findActiveByCampaignIdAndStationId(campaign.getId(), s.id()).orElse(null);
            stations.add(StampBookView.StationCellView.builder()
                    .stationId(s.id())
                    .stationName(s.name())
                    .sequence(s.sequence())
                    .collected(isCollected)
                    .stampDesignUrl(design != null ? design.getArtworkUrl() : null)
                    .build());
        }

        return StampBookView.builder()
                .lineId(lineId)
                .campaignId(campaign.getId())
                .stations(stations)
                .build();
    }

    public ProgressView computeProgress(UUID userId, UUID lineId, UUID campaignId) {
        long collected = userStampRepository.countDistinctStationsByUserIdAndCampaignId(userId, campaignId);
        long total = stationReadPort.listActiveStationsByLineId(lineId).size();
        int pct = total <= 0 ? 0 : (int) Math.floor((collected * 100.0) / total);
        return ProgressView.builder()
                .lineId(lineId)
                .collected(collected)
                .total(total)
                .percentage(pct)
                .build();
    }

    private List<UserStampView> mapUserStamps(List<UserStamp> stamps) {
        List<UserStampView> res = new ArrayList<>();
        for (UserStamp us : stamps) {
            MetroStationView station = stationReadPort.getStationViewById(us.getStationId());
            StampDesign design = stampDesignRepository.findById(us.getStampDesignId()).orElse(null);
            res.add(userStampAppMapper.toUserStampResponse(us, station, design));
        }
        return res;
    }

    private Campaign resolveCampaign(UUID lineId, UUID campaignId) {
        if (campaignId != null) {
            return campaignRepository.findById(campaignId).orElseThrow(() -> new CampaignNotFoundException(campaignId));
        }
        return campaignRepository.findDefaultByLineId(lineId)
                .orElseThrow(() -> new CampaignNotFoundException(null));
    }
}

