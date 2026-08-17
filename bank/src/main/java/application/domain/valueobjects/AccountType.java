package application.domain.valueobjects;

public final class AccountType extends DomainCatalog {

    public static final AccountType SAVINGS = new AccountType(
            "SAVINGS", "Savings Account", "Standard interest-bearing deposit account.");
    public static final AccountType CHECKING = new AccountType(
            "CHECKING", "Checking Account", "Transaction account intended for frequent operations.");
    public static final AccountType BUSINESS = new AccountType(
            "BUSINESS", "Business Account", "Account designed for corporate customers.");

    private AccountType(String code, String name, String description) {
        super(code, name, description);
    }
}
