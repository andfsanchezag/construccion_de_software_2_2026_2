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

    public static AccountType fromCode(String code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case "SAVINGS" -> SAVINGS;
            case "CHECKING" -> CHECKING;
            case "BUSINESS" -> BUSINESS;
            default -> throw new IllegalArgumentException("Unknown AccountType code: " + code);
        };
    }
}
