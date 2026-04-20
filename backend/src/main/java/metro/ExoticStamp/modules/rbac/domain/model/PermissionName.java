package metro.ExoticStamp.modules.rbac.domain.model;

public enum PermissionName {
    // Sales
    READ_SALES,
    UPLOAD_SALES,

    // Customer
    READ_CUSTOMER,
    UPLOAD_CUSTOMER,

    // Service
    READ_SERVICE,
    UPLOAD_SERVICE,

    // Booking
    READ_BOOKING,
    UPLOAD_BOOKING,

    // Realtime
    READ_REALTIME,

    // User management
    MANAGE_USER,
    ASSIGN_ROLE,

    // App usage
    UPLOAD_APP_USAGE

    // Dùng trong @PreAuthorize:
    // @PreAuthorize("hasAuthority('READ_SALES')")
}