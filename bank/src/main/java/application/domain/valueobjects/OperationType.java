package application.domain.valueobjects;

public final class OperationType extends DomainCatalog {

    // Account operations
    public static final OperationType ACCOUNT_OPENING = new OperationType(
            "ACCOUNT_OPENING", "Account Opening", "Creation of a new bank account.");
    public static final OperationType DEPOSIT = new OperationType(
            "DEPOSIT", "Deposit", "Deposit of funds into an account.");
    public static final OperationType WITHDRAWAL = new OperationType(
            "WITHDRAWAL", "Withdrawal", "Withdrawal of funds from an account.");
    public static final OperationType ACCOUNT_BLOCKING = new OperationType(
            "ACCOUNT_BLOCKING", "Account Blocking", "Blocking of a bank account.");
    public static final OperationType ACCOUNT_UNBLOCKING = new OperationType(
            "ACCOUNT_UNBLOCKING", "Account Unblocking", "Removal of a block from a bank account.");
    public static final OperationType ACCOUNT_CLOSING = new OperationType(
            "ACCOUNT_CLOSING", "Account Closing", "Permanent closure of a bank account.");

    // Transfer operations
    public static final OperationType TRANSFER_CREATION = new OperationType(
            "TRANSFER_CREATION", "Transfer Creation", "Creation of a transfer request.");
    public static final OperationType TRANSFER_APPROVAL = new OperationType(
            "TRANSFER_APPROVAL", "Transfer Approval", "Approval of a transfer requiring authorization.");
    public static final OperationType TRANSFER_REJECTION = new OperationType(
            "TRANSFER_REJECTION", "Transfer Rejection", "Rejection of a transfer request.");
    public static final OperationType TRANSFER_EXECUTION = new OperationType(
            "TRANSFER_EXECUTION", "Transfer Execution", "Successful execution of a transfer.");
    public static final OperationType TRANSFER_EXPIRATION = new OperationType(
            "TRANSFER_EXPIRATION", "Transfer Expiration", "Expiration of the transfer approval or execution window.");

    // Loan operations
    public static final OperationType LOAN_APPLICATION = new OperationType(
            "LOAN_APPLICATION", "Loan Application", "Submission of a loan request.");
    public static final OperationType LOAN_APPROVAL = new OperationType(
            "LOAN_APPROVAL", "Loan Approval", "Approval of a loan request.");
    public static final OperationType LOAN_REJECTION = new OperationType(
            "LOAN_REJECTION", "Loan Rejection", "Rejection of a loan request.");
    public static final OperationType LOAN_DISBURSEMENT = new OperationType(
            "LOAN_DISBURSEMENT", "Loan Disbursement", "Transfer of approved loan funds to the destination account.");
    public static final OperationType LOAN_PAYMENT = new OperationType(
            "LOAN_PAYMENT", "Loan Payment", "Registration of a payment made against a loan.");
    public static final OperationType LOAN_OVERDUE = new OperationType(
            "LOAN_OVERDUE", "Loan Overdue", "Loan marked as overdue due to unmet obligations.");
    public static final OperationType LOAN_CANCELLATION = new OperationType(
            "LOAN_CANCELLATION", "Loan Cancellation", "Cancellation of a loan in an eligible state.");

    private OperationType(String code, String name, String description) {
        super(code, name, description);
    }
}
