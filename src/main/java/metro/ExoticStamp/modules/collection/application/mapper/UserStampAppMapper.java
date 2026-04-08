package metro.ExoticStamp.modules.collection.application.mapper;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.domain.model.CollectMethod;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.collection.presentation.response.UserStampResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationDetailResponse;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserStampAppMapper {

    public UserStampResponse toUserStampResponse(
            UserStamp userStamp,
            StationDetailResponse station,
            StampDesign stampDesign
    ) {
        return UserStampResponse.builder()
                .stampId(userStamp.getId())
                .stationId(userStamp.getStationId())
                .lineId(station.getLineId())
                .campaignId(userStamp.getCampaignId())
                .stationName(station.getName())
                .stampDesignUrl(stampDesign != null ? stampDesign.getArtworkUrl() : null)
                .collectedAt(userStamp.getCollectedAt())
                .collectMethod(userStamp.getCollectMethod() != null ? userStamp.getCollectMethod().name() : null)
                .build();
    }

    public CollectMethod resolveCollectMethod(String nfcTagId, String qrToken) {
        if (nfcTagId != null && !nfcTagId.isBlank()) return CollectMethod.NFC;
        return CollectMethod.QR;
    }
}

