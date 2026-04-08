package metro.ExoticStamp.modules.collection.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.application.mapper.UserStampAppMapper;
import metro.ExoticStamp.modules.collection.application.port.UserStampCachePort;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignNotFoundException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.UserStampRepository;
import metro.ExoticStamp.modules.collection.presentation.response.ProgressResponse;
import metro.ExoticStamp.modules.collection.presentation.response.StampBookResponse;
import metro.ExoticStamp.modules.collection.presentation.response.UserStampResponse;
import metro.ExoticStamp.modules.metro.application.LineQueryService;
import metro.ExoticStamp.modules.metro.application.StationQueryService;
import metro.ExoticStamp.modules.metro.presentation.dto.response.LineDetailResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationDetailResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationResponse;
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
    private final StationQueryService stationQueryService;
    private final LineQueryService lineQueryService;
    private final UserStampCachePort cachePort;
    private final UserStampAppMapper userStampAppMapper;

    public List<UserStampResponse> getMyStamps(UUID userId, UUID lineId, UUID campaignId) {
        Campaign campaign = resolveCampaign(lineId, campaignId);

        UUID effectiveLineId = campaign.getLineId() != null ? campaign.getLineId() : lineId;
        boolean cacheable = campaignId == null;
        if (cacheable) {
            Optional<List<UserStampResponse>> cached = cachePort.getUserStamps(userId, effectiveLineId);
            if (cached.isPresent()) return cached.get();
        }

        List<UserStamp> stamps = userStampRepository.findByUserIdAndCampaignId(userId, campaign.getId());
        List<UserStampResponse> res = mapUserStamps(stamps);

        if (cacheable) {
            cachePort.putUserStamps(userId, effectiveLineId, res);
        }
        return res;
    }

    public ProgressResponse getMyProgress(UUID userId, UUID lineId, UUID campaignId) {
        Campaign campaign = resolveCampaign(lineId, campaignId);
        UUID effectiveLineId = campaign.getLineId() != null ? campaign.getLineId() : lineId;
        boolean cacheable = campaignId == null;
        if (cacheable) {
            Optional<ProgressResponse> cached = cachePort.getUserProgress(userId, effectiveLineId);
            if (cached.isPresent()) return cached.get();
        }

        ProgressResponse computed = computeProgress(userId, effectiveLineId, campaign.getId());
        if (cacheable) {
            cachePort.putUserProgress(userId, effectiveLineId, computed);
        }
        return computed;
    }

    public List<UserStampResponse> getMyHistory(UUID userId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        List<UserStamp> stamps = userStampRepository.findRecentByUserId(userId, safeLimit);
        return mapUserStamps(stamps);
    }

    public StampBookResponse getStampBook(UUID userId, UUID lineId, UUID campaignId) {
        Campaign campaign = resolveCampaign(lineId, campaignId);

        LineDetailResponse line = lineQueryService.getLineDetail(lineId, true);
        List<UserStamp> collected = userStampRepository.findByUserIdAndCampaignId(userId, campaign.getId());
        Set<UUID> collectedStationIds = collected.stream().map(UserStamp::getStationId).collect(HashSet::new, Set::add, Set::addAll);

        List<StampBookResponse.StampBookStationResponse> stations = new ArrayList<>();
        for (StationResponse s : line.getStations()) {
            boolean isCollected = collectedStationIds.contains(s.getId());
            StampDesign design = stampDesignRepository.findActiveByCampaignIdAndStationId(campaign.getId(), s.getId()).orElse(null);
            stations.add(StampBookResponse.StampBookStationResponse.builder()
                    .stationId(s.getId())
                    .stationName(s.getName())
                    .sequence(s.getSequence())
                    .collected(isCollected)
                    .stampDesignUrl(design != null ? design.getArtworkUrl() : null)
                    .build());
        }

        return StampBookResponse.builder()
                .lineId(lineId)
                .campaignId(campaign.getId())
                .stations(stations)
                .build();
    }

    public ProgressResponse computeProgress(UUID userId, UUID lineId, UUID campaignId) {
        long collected = userStampRepository.countDistinctStationsByUserIdAndCampaignId(userId, campaignId);
        LineDetailResponse line = lineQueryService.getLineDetail(lineId, true);
        long total = line.getStations() != null ? line.getStations().size() : 0;
        int pct = total <= 0 ? 0 : (int) Math.floor((collected * 100.0) / total);
        return ProgressResponse.builder()
                .lineId(lineId)
                .collected(collected)
                .total(total)
                .percentage(pct)
                .build();
    }

    private List<UserStampResponse> mapUserStamps(List<UserStamp> stamps) {
        List<UserStampResponse> res = new ArrayList<>();
        for (UserStamp us : stamps) {
            StationDetailResponse station = stationQueryService.getStationDetailById(us.getStationId());
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

