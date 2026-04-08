package metro.ExoticStamp.modules.rbac.application;

import metro.ExoticStamp.config.RbacProperties;
import metro.ExoticStamp.modules.auth.application.AuditLogService;
import metro.ExoticStamp.modules.rbac.application.command.RevokeRoleCommand;
import metro.ExoticStamp.modules.rbac.application.support.RbacSecurityContextHelper;
import metro.ExoticStamp.modules.rbac.domain.exception.LastAdminProtectionException;
import metro.ExoticStamp.modules.rbac.domain.model.Role;
import metro.ExoticStamp.modules.rbac.domain.model.RoleStatus;
import metro.ExoticStamp.modules.rbac.domain.repository.RoleRepository;
import metro.ExoticStamp.modules.rbac.domain.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleCommandServiceTest {

    private static final UUID USER = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ADMIN_ROLE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RbacProperties rbacProperties;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private RbacSecurityContextHelper securityContextHelper;

    @InjectMocks
    private RoleCommandService roleCommandService;

    @Test
    void revokeRole_whenLastActiveAdmin_throws() {
        when(rbacProperties.getAdminRoleCode()).thenReturn("ADMIN");
        when(rbacProperties.getMaxRoleCodeLength()).thenReturn(64);
        Role admin = Role.builder()
                .id(ADMIN_ROLE_ID)
                .role("ADMIN")
                .status(RoleStatus.ACTIVE)
                .systemRole(true)
                .build();
        when(roleRepository.findByRoleCode("ADMIN")).thenReturn(Optional.of(admin));
        when(userRoleRepository.existsByUserIdAndRoleId(USER, ADMIN_ROLE_ID)).thenReturn(true);
        when(userRoleRepository.countActiveUsersWithRoleCode("ADMIN")).thenReturn(1L);

        assertThrows(LastAdminProtectionException.class, () -> roleCommandService.revokeRole(
                RevokeRoleCommand.builder().userId(USER).roleName("ADMIN").build()));
    }
}
