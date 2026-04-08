package metro.ExoticStamp.modules.metro.application.port;

import java.util.UUID;

import metro.ExoticStamp.modules.metro.presentation.dto.response.StationDetailResponse;

import java.util.Optional;

public interface StationCachePort {

    Optional<StationDetailResponse> getByNfcTagId(String nfcTagId);

    Optional<StationDetailResponse> getByQrToken(String qrToken);

    Optional<StationDetailResponse> getByStationId(UUID stationId);

    void putByNfcTagId(String nfcTagId, StationDetailResponse value);

    void putByQrToken(String qrToken, StationDetailResponse value);

    void putByStationId(UUID stationId, StationDetailResponse value);

    void evictByNfcTagId(String nfcTagId);

    void evictByQrToken(String qrToken);

    void evictDetailByStationId(UUID stationId);
}



