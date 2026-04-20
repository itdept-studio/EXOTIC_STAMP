package metro.ExoticStamp.modules.collection.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.collection.application.mapper.UserStampAppMapper;
import metro.ExoticStamp.modules.collection.application.port.UserStampCachePort;
import metro.ExoticStamp.modules.collection.application.view.ProgressView;
import metro.ExoticStamp.modules.collection.application.view.StampBookView;
import metro.ExoticStamp.modules.collection.application.view.UserStampView;
import metro.ExoticStamp.modules.collection.config.CollectionProperties;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignNotFoundException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.collection.domain.model.UserStampSlice;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.UserStampRepository;
import metro.ExoticStamp.modules.metro.application.port.StationReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroStationView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Read-side collection use cases: stamps, progress, history, stamp book (cached, batch-mapped).
 */
@Slf4j
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
    private final CollectionProperties collectionProperties;

    /**
     * Paginated stamps for a user on a line/campaign.
     */
    public PageResponse<UserStampView> getMyStamps(UUID userId, UUID lineId, UUID campaignId, int page, int size) {
        int p = Math.max(0, page);
        int s = normalizeSize(size);
        Campaign campaign = resolveCampaign(lineId, campaignId);
        UUID effectiveLineId = campaign.getLineId() != null ? campaign.getLineId() : lineId;

        Optional<PageResponse<UserStampView>> cached = cachePort.getUserStamps(userId, effectiveLineId, campaign.getId(), p, s);
        if (cached.isPresent()) {
            return cached.get();
        }

        UserStampSlice slice = userStampRepository.findByUserIdAndCampaignIdPaged(userId, campaign.getId(), p, s);
        List<UserStampView> mapped = mapUserStamps(slice.content());
        PageResponse<UserStampView> res = PageResponse.of(
                mapped,
                slice.totalElements(),
                slice.totalPages(),
                slice.page(),
                slice.size()
        );
        cachePort.putUserStamps(userId, effectiveLineId, campaign.getId(), p, s, res);
        return res;
    }

    public ProgressView getMyProgress(UUID userId, UUID lineId, UUID campaignId) {
        Campaign campaign = resolveCampaign(lineId, campaignId);
        UUID effectiveLineId = campaign.getLineId() != null ? campaign.getLineId() : lineId;

        Optional<ProgressView> cached = cachePort.getUserProgress(userId, effectiveLineId);
        if (cached.isPresent()) {
            return cached.get();
        }

        ProgressView computed = computeProgress(userId, effectiveLineId, campaign.getId());
        cachePort.putUserProgress(userId, effectiveLineId, computed);
        return computed;
    }

    /**
     * Paginated recent stamps across all campaigns for the user.
     */
    public PageResponse<UserStampView> getMyHistory(UUID userId, int page, int size) {
        int p = Math.max(0, page);
        int s = normalizeSize(size);

        Optional<PageResponse<UserStampView>> cached = cachePort.getUserHistory(userId, p, s);
        if (cached.isPresent()) {
            return cached.get();
        }

        UserStampSlice slice = userStampRepository.findByUserIdPaged(userId, p, s);
        List<UserStampView> mapped = mapUserStamps(slice.content());
        PageResponse<UserStampView> res = PageResponse.of(
                mapped,
                slice.totalElements(),
                slice.totalPages(),
                slice.page(),
                slice.size()
        );
        cachePort.putUserHistory(userId, p, s, res);
        return res;
    }

    public StampBookView getStampBook(UUID userId, UUID lineId, UUID campaignId) {
        Campaign campaign = resolveCampaign(lineId, campaignId);

        Optional<StampBookView> cached = cachePort.getStampBook(userId, lineId, campaign.getId());
        if (cached.isPresent()) {
            return cached.get();
        }

        List<MetroStationView> lineStations = stationReadPort.listActiveStationsByLineId(lineId);
        List<UserStamp> collected = userStampRepository.findByUserIdAndCampaignId(userId, campaign.getId());
        Set<UUID> collectedStationIds = collected.stream().map(UserStamp::getStationId).collect(Collectors.toSet());

        List<UUID> stationIds = lineStations.stream().map(MetroStationView::id).toList();
        List<StampDesign> designs = stampDesignRepository.findActiveByCampaignIdAndStationIdIn(campaign.getId(), stationIds);
        Map<UUID, StampDesign> designByStation = designs.stream()
                .filter(d -> d.getStationId() != null)
                .collect(Collectors.toMap(StampDesign::getStationId, Function.identity(), (a, b) -> a));

        List<StampBookView.StationCellView> stations = new ArrayList<>();
        for (MetroStationView s : lineStations) {
            boolean isCollected = collectedStationIds.contains(s.id());
            StampDesign design = designByStation.get(s.id());
            stations.add(StampBookView.StationCellView.builder()
                    .stationId(s.id())
                    .stationName(s.name())
                    .sequence(s.sequence())
                    .collected(isCollected)
                    .stampDesignUrl(design != null ? design.getArtworkUrl() : null)
                    .build());
        }

        StampBookView view = StampBookView.builder()
                .lineId(lineId)
                .campaignId(campaign.getId())
                .stations(stations)
                .build();
        cachePort.putStampBook(userId, lineId, campaign.getId(), view);
        return view;
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

    private int normalizeSize(int size) {
        int def = collectionProperties.getDefaultPageSize();
        int max = collectionProperties.getMaxPageSize();
        if (size <= 0) {
            return def;
        }
        return Math.min(size, max);
    }

    private List<UserStampView> mapUserStamps(List<UserStamp> stamps) {
        if (stamps.isEmpty()) {
            return List.of();
        }
        Set<UUID> stationIds = stamps.stream().map(UserStamp::getStationId).collect(Collectors.toSet());
        Set<UUID> designIds = stamps.stream().map(UserStamp::getStampDesignId).collect(Collectors.toSet());

        Map<UUID, MetroStationView> stationById = stationReadPort.listStationViewsByIds(stationIds).stream()
                .collect(Collectors.toMap(MetroStationView::id, Function.identity(), (a, b) -> a));
        Map<UUID, StampDesign> designById = stampDesignRepository.findAllByIdIn(designIds).stream()
                .collect(Collectors.toMap(StampDesign::getId, Function.identity(), (a, b) -> a));

        List<UserStampView> res = new ArrayList<>(stamps.size());
        for (UserStamp us : stamps) {
            MetroStationView station = stationById.get(us.getStationId());
            StampDesign design = designById.get(us.getStampDesignId());
            if (station == null) {
                log.warn("[CollectionQuery] missing station view for stationId={}", us.getStationId());
                continue;
            }
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
