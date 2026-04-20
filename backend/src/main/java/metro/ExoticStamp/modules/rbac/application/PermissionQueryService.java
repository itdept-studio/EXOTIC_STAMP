package metro.ExoticStamp.modules.rbac.application;

import metro.ExoticStamp.modules.rbac.application.mapper.RoleAppMapper;
import metro.ExoticStamp.modules.rbac.domain.repository.PermissionRepository;
import metro.ExoticStamp.modules.rbac.presentation.dto.response.PermissionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionQueryService {

    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<PermissionResponse> listPermissions() {
        return permissionRepository.findAllOrderByPermission().stream()
                .map(RoleAppMapper::toPermissionResponse)
                .toList();
    }
}
