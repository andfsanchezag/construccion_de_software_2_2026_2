package application.domain.valueobjects;

public final class AccountStatus extends DomainCatalog {

    public static final AccountStatus ACTIVE = new AccountStatus(
            "ACTIVE", "Active", "Account is fully operational and may perform authorized transactions.");
    public static final AccountStatus BLOCKED = new AccountStatus(
            "BLOCKED", "Blocked", "Transactions are temporarily disabled.");
    public static final AccountStatus CLOSED = new AccountStatus(
            "CLOSED", "Closed", "Account has been permanently closed.");

    private AccountStatus(String code, String name, String description) {
        super(code, name, description);
    }
}
