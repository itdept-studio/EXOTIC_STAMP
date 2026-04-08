package metro.ExoticStamp.modules.collection.application.port;

import metro.ExoticStamp.modules.collection.presentation.response.ProgressResponse;
import metro.ExoticStamp.modules.collection.presentation.response.UserStampResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStampCachePort {

    Optional<List<UserStampResponse>> getUserStamps(UUID userId, UUID lineId);

    void putUserStamps(UUID userId, UUID lineId, List<UserStampResponse> value);

    void evictUserStamps(UUID userId, UUID lineId);

    Optional<ProgressResponse> getUserProgress(UUID userId, UUID lineId);

    void putUserProgress(UUID userId, UUID lineId, ProgressResponse value);

    void evictUserProgress(UUID userId, UUID lineId);
}

