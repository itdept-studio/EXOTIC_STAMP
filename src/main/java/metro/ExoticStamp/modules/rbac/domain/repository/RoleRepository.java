package metro.ExoticStamp.modules.rbac.domain.repository;

import metro.ExoticStamp.modules.rbac.domain.model.Role;
import metro.ExoticStamp.modules.rbac.domain.model.RoleName;

import java.util.List;
import java.util.Optional;

// Domain interface — không import Spring/JPA
public interface RoleRepository {
    Optional<Role> findById(Integer id);

    Optional<Role> findByRoleName(RoleName roleName);

    Optional<Role> findByRoleCode(String normalizedRoleCode);

    Optional<Role> findByIdWithPermissions(Integer id);

    List<Role> findAll();

    Role save(Role role);
}