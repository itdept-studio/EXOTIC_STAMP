package metro.ExoticStamp.modules.user.application;

import metro.ExoticStamp.common.model.PageQuery;
import metro.ExoticStamp.common.model.PageResult;
import metro.ExoticStamp.modules.user.application.mapper.UserAppMapper;
import metro.ExoticStamp.modules.user.application.port.UserCachePort;
import metro.ExoticStamp.modules.user.domain.exception.UserNotFoundException;
import metro.ExoticStamp.modules.user.domain.model.User;
import metro.ExoticStamp.modules.user.domain.repository.UserRepository;
import metro.ExoticStamp.modules.user.presentation.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;
    private final UserCachePort cachePort;

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return cachePort.get(id)
                .orElseGet(() -> {
                    UserResponse res = userRepository.findById(id)
                            .map(UserAppMapper::toResponse)
                            .orElseThrow(() -> new UserNotFoundException(id));
                    cachePort.put(id, res);
                    return res;
                });
    }

    @Transactional(readOnly = true)
    public UserResponse getByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserAppMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException("username", username));
    }

    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserAppMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException("email", email));
    }

    @Transactional(readOnly = true)
    public PageResult<UserResponse> getAll(PageQuery query) {
        PageResult<User> page = userRepository.findAll(query);
        List<UserResponse> responses = page.content().stream()
                .map(UserAppMapper::toResponse)
                .toList();
        return PageResult.of(responses, page.totalElements(), page.totalPages(), page.currentPage());
    }
}
