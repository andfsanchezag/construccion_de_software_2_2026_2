package application.domain.valueobjects;

public final class LoanType extends DomainCatalog {

    public static final LoanType PERSONAL = new LoanType(
            "PERSONAL", "Personal Loan", "Loan intended for personal use.");
    public static final LoanType MORTGAGE = new LoanType(
            "MORTGAGE", "Mortgage Loan", "Loan secured by real estate.");
    public static final LoanType VEHICLE = new LoanType(
            "VEHICLE", "Vehicle Loan", "Loan used to finance vehicle purchases.");
    public static final LoanType BUSINESS = new LoanType(
            "BUSINESS", "Business Loan", "Loan intended for business financing.");

    private LoanType(String code, String name, String description) {
        super(code, name, description);
    }

    public static LoanType fromCode(String code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case "PERSONAL" -> PERSONAL;
            case "MORTGAGE" -> MORTGAGE;
            case "VEHICLE" -> VEHICLE;
            case "BUSINESS" -> BUSINESS;
            default -> throw new IllegalArgumentException("Unknown LoanType code: " + code);
        };
    }
}
