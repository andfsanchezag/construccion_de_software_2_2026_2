package application.domain.valueobjects;

public final class CustomerStatus extends DomainCatalog {

    public static final CustomerStatus ACTIVE = new CustomerStatus(
            "ACTIVE", "Active", "Customer maintains an active banking relationship.");
    public static final CustomerStatus INACTIVE = new CustomerStatus(
            "INACTIVE", "Inactive", "Customer exists but is not currently active for normal banking operations.");
    public static final CustomerStatus BLOCKED = new CustomerStatus(
            "BLOCKED", "Blocked", "Customer's banking relationship has been suspended.");

    private CustomerStatus(String code, String name, String description) {
        super(code, name, description);
    }
}
