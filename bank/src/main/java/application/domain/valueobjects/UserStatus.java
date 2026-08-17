package application.domain.valueobjects;

public final class UserStatus extends DomainCatalog {

    public static final UserStatus ACTIVE = new UserStatus(
            "ACTIVE", "Active", "User can access the system normally.");
    public static final UserStatus INACTIVE = new UserStatus(
            "INACTIVE", "Inactive", "User exists but cannot perform system operations.");
    public static final UserStatus BLOCKED = new UserStatus(
            "BLOCKED", "Blocked", "User access has been suspended.");

    private UserStatus(String code, String name, String description) {
        super(code, name, description);
    }
}
