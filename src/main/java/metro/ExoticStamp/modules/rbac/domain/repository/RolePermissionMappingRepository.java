package metro.ExoticStamp.modules.rbac.domain.repository;

import metro.ExoticStamp.modules.rbac.domain.model.RolePermission;

public interface RolePermissionMappingRepository {

    RolePermission save(RolePermission mapping);

    boolean existsByRoleIdAndPermissionId(Integer roleId, Integer permissionId);

    void deleteByRoleIdAndPermissionId(Integer roleId, Integer permissionId);
}
