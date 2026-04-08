package metro.ExoticStamp.modules.rbac.infrastructure;

import metro.ExoticStamp.modules.rbac.domain.model.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRolePermissionRepository extends JpaRepository<RolePermission, Integer> {

    boolean existsByRole_IdAndPermission_Id(Integer roleId, Integer permissionId);

    void deleteByRole_IdAndPermission_Id(Integer roleId, Integer permissionId);
}
