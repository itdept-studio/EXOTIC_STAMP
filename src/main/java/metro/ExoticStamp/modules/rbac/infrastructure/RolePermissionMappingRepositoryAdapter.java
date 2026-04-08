package metro.ExoticStamp.modules.rbac.infrastructure;

import metro.ExoticStamp.modules.rbac.domain.model.RolePermission;
import metro.ExoticStamp.modules.rbac.domain.repository.RolePermissionMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RolePermissionMappingRepositoryAdapter implements RolePermissionMappingRepository {

    private final JpaRolePermissionRepository jpa;

    @Override
    public RolePermission save(RolePermission mapping) {
        return jpa.save(mapping);
    }

    @Override
    public boolean existsByRoleIdAndPermissionId(Integer roleId, Integer permissionId) {
        return jpa.existsByRole_IdAndPermission_Id(roleId, permissionId);
    }

    @Override
    public void deleteByRoleIdAndPermissionId(Integer roleId, Integer permissionId) {
        jpa.deleteByRole_IdAndPermission_Id(roleId, permissionId);
    }
}
