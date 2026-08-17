package application.domain.valueobjects;

public final class SystemRole extends DomainCatalog {

    public static final SystemRole NATURAL_CUSTOMER = new SystemRole(
            "NATURAL_CUSTOMER", "Natural Customer", "Individual banking customer.");
    public static final SystemRole BUSINESS_CUSTOMER = new SystemRole(
            "BUSINESS_CUSTOMER", "Business Customer", "Corporate banking customer.");
    public static final SystemRole TELLER_EMPLOYEE = new SystemRole(
            "TELLER_EMPLOYEE", "Teller Employee", "Employee responsible for performing branch operations.");
    public static final SystemRole COMMERCIAL_EMPLOYEE = new SystemRole(
            "COMMERCIAL_EMPLOYEE", "Commercial Employee", "Employee responsible for customer relationships and loan-related activities.");
    public static final SystemRole BUSINESS_OPERATOR = new SystemRole(
            "BUSINESS_OPERATOR", "Business Operator", "User authorized to perform operations on behalf of business customers.");
    public static final SystemRole BUSINESS_SUPERVISOR = new SystemRole(
            "BUSINESS_SUPERVISOR", "Business Supervisor", "User authorized to approve business transfers requiring authorization.");
    public static final SystemRole INTERNAL_ANALYST = new SystemRole(
            "INTERNAL_ANALYST", "Internal Analyst", "User responsible for reviewing and approving loan applications.");

    private SystemRole(String code, String name, String description) {
        super(code, name, description);
    }
}
