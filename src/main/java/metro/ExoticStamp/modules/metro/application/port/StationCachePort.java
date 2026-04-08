package metro.ExoticStamp.modules.metro.application.port;

import metro.ExoticStamp.modules.metro.presentation.dto.response.StationDetailResponse;

import java.util.Optional;

public interface StationCachePort {

    Optional<StationDetailResponse> getByNfcTagId(String nfcTagId);

    Optional<StationDetailResponse> getByQrToken(String qrToken);

    Optional<StationDetailResponse> getByStationId(Integer stationId);

    void putByNfcTagId(String nfcTagId, StationDetailResponse value);

    void putByQrToken(String qrToken, StationDetailResponse value);

    void putByStationId(Integer stationId, StationDetailResponse value);

    void evictByNfcTagId(String nfcTagId);

    void evictByQrToken(String qrToken);

    void evictDetailByStationId(Integer stationId);
}
