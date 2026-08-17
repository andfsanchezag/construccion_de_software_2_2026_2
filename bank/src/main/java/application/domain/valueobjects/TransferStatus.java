package application.domain.valueobjects;

public final class TransferStatus extends DomainCatalog {

    public static final TransferStatus PENDING = new TransferStatus(
            "PENDING", "Pending", "Transfer has been created and is pending processing.");
    public static final TransferStatus WAITING_FOR_APPROVAL = new TransferStatus(
            "WAITING_FOR_APPROVAL", "Waiting for Approval", "Transfer requires managerial or authorized approval before execution.");
    public static final TransferStatus APPROVED = new TransferStatus(
            "APPROVED", "Approved", "Transfer has been approved and is ready for execution.");
    public static final TransferStatus EXECUTED = new TransferStatus(
            "EXECUTED", "Executed", "Funds have been successfully transferred.");
    public static final TransferStatus REJECTED = new TransferStatus(
            "REJECTED", "Rejected", "Transfer request has been denied.");
    public static final TransferStatus EXPIRED = new TransferStatus(
            "EXPIRED", "Expired", "The approval or execution time window has expired.");

    private TransferStatus(String code, String name, String description) {
        super(code, name, description);
    }
}
