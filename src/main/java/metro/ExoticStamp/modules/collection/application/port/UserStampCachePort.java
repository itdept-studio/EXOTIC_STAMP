package metro.ExoticStamp.modules.collection.application.port;

import metro.ExoticStamp.modules.collection.application.view.ProgressView;
import metro.ExoticStamp.modules.collection.application.view.UserStampView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStampCachePort {

    Optional<List<UserStampView>> getUserStamps(UUID userId, UUID lineId);

    void putUserStamps(UUID userId, UUID lineId, List<UserStampView> value);

    void evictUserStamps(UUID userId, UUID lineId);

    Optional<ProgressView> getUserProgress(UUID userId, UUID lineId);

    void putUserProgress(UUID userId, UUID lineId, ProgressView value);

    void evictUserProgress(UUID userId, UUID lineId);
}

