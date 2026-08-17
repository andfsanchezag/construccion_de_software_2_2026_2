package application.domain.valueobjects;

public final class LoanStatus extends DomainCatalog {

    public static final LoanStatus UNDER_REVIEW = new LoanStatus(
            "UNDER_REVIEW", "Under Review", "Loan request is under evaluation.");
    public static final LoanStatus APPROVED = new LoanStatus(
            "APPROVED", "Approved", "Loan has been approved but funds have not yet been disbursed.");
    public static final LoanStatus REJECTED = new LoanStatus(
            "REJECTED", "Rejected", "Loan request has been rejected.");
    public static final LoanStatus DISBURSED = new LoanStatus(
            "DISBURSED", "Disbursed", "Approved funds have been transferred to the destination account.");
    public static final LoanStatus OVERDUE = new LoanStatus(
            "OVERDUE", "Overdue", "Loan has active obligations that have not been met on time.");
    public static final LoanStatus CANCELLED = new LoanStatus(
            "CANCELLED", "Cancelled", "Loan has been cancelled and is no longer active.");

    private LoanStatus(String code, String name, String description) {
        super(code, name, description);
    }
}
