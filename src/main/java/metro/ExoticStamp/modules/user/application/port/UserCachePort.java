package metro.ExoticStamp.modules.user.application.port;

import metro.ExoticStamp.modules.user.presentation.dto.response.UserResponse;

import java.util.Optional;
import java.util.UUID;

public interface UserCachePort {
    void put(UUID id, UserResponse response);
    Optional<UserResponse> get(UUID id);
    void evict(UUID id);
}
