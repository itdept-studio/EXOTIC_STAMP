package metro.ExoticStamp.modules.rbac.domain.exception;

public class PermissionNotFoundException extends RuntimeException {

    public PermissionNotFoundException(Integer id) {
        super("Permission not found with id: " + id);
    }

    public PermissionNotFoundException(String code) {
        super("Permission not found with code: " + code);
    }
}
