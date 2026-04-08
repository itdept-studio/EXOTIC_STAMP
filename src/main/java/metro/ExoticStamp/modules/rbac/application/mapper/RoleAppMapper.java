package metro.ExoticStamp.modules.rbac.application.mapper;

import metro.ExoticStamp.modules.rbac.application.command.AssignRoleCommand;
import metro.ExoticStamp.modules.rbac.application.command.RevokeRoleCommand;
import metro.ExoticStamp.modules.rbac.domain.model.Permission;
import metro.ExoticStamp.modules.rbac.domain.model.Role;
import metro.ExoticStamp.modules.rbac.presentation.dto.request.AssignRoleRequest;
import metro.ExoticStamp.modules.rbac.presentation.dto.request.RevokeRoleRequest;
import metro.ExoticStamp.modules.rbac.presentation.dto.response.PermissionResponse;
import metro.ExoticStamp.modules.rbac.presentation.dto.response.RoleResponse;

public final class RoleAppMapper {

    private RoleAppMapper() {}

    public static AssignRoleCommand toAssignCommand(AssignRoleRequest r) {
        return AssignRoleCommand.builder()
                .userId(r.getUserId())
                .roleName(r.getRoleName())
                .build();
    }

    public static RevokeRoleCommand toRevokeCommand(RevokeRoleRequest r) {
        return RevokeRoleCommand.builder()
                .userId(r.getUserId())
                .roleName(r.getRoleName())
                .build();
    }

    public static RoleResponse toRoleResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .role(role.getRole())
                .description(role.getDescription())
                .status(role.getStatus() == null ? null : role.getStatus().name())
                .systemRole(role.isSystemRole())
                .build();
    }

    public static PermissionResponse toPermissionResponse(Permission p) {
        return PermissionResponse.builder()
                .id(p.getId())
                .permission(p.getPermission())
                .description(p.getDescription())
                .build();
    }
}