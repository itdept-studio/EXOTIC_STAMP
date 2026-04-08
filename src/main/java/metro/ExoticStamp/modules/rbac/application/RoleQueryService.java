package metro.ExoticStamp.modules.rbac.application;

import metro.ExoticStamp.modules.rbac.application.mapper.RoleAppMapper;
import metro.ExoticStamp.modules.rbac.domain.exception.RoleNotFoundException;
import metro.ExoticStamp.modules.rbac.domain.model.Role;
import metro.ExoticStamp.modules.rbac.domain.repository.RoleRepository;
import metro.ExoticStamp.modules.rbac.domain.repository.UserRoleRepository;
import metro.ExoticStamp.modules.rbac.presentation.dto.response.PermissionResponse;
import metro.ExoticStamp.modules.rbac.presentation.dto.response.RoleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleQueryService {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Transactional(readOnly = true)
    public RoleResponse getRoleById(UUID roleId) {
        return roleRepository.findById(roleId)
                .map(RoleAppMapper::toRoleResponse)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll()
            .stream()
            .map(RoleAppMapper::toRoleResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getRolesByUserId(UUID userId) {
        return userRoleRepository.findAllByUserId(userId)
            .stream()
            .map(ur -> RoleAppMapper.toRoleResponse(ur.getRole()))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissionsByRoleId(UUID roleId) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
            .orElseThrow(() -> new RoleNotFoundException(roleId));
        return role.getRolePermissions()
            .stream()
            .map(rp -> RoleAppMapper.toPermissionResponse(rp.getPermission()))
            .toList();
    }

    /** Used by auth (JWT claims) and security filter (authorities). */
    @Transactional(readOnly = true)
    public List<String> getRoleNamesByUserId(UUID userId) {
        return userRoleRepository.findAllByUserId(userId)
            .stream()
            .map(ur -> ur.getRole().getRole())
            .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getPermissionCodesByUserId(UUID userId) {
        return userRoleRepository.findDistinctPermissionCodesByUserId(userId);
    }
}
