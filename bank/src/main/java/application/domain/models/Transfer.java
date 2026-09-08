package application.domain.models;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.valueobjects.TransferStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Transfer extends BankingProduct {
    private BankAccount sourceAccount;
    private BankAccount destinationAccount;
    private BigDecimal amount;
    private LocalDateTime creationDate;
    private LocalDateTime approvalDate;
    private TransferStatus transferStatus;
    private User createdBy;
    private User approvedBy;

    private String statusCode() {
        return transferStatus == null ? "UNKNOWN" : transferStatus.getCode();
    }

    /**
     * Assigns the initial status after creation.
     * Accepts a new transfer (null status) or a PENDING transfer and moves it to
     * WAITING_FOR_APPROVAL when approval is required, or APPROVED otherwise.
     * The approval decision itself (threshold comparison) stays in the service
     * through BusinessConfigurationPort; the model only guards the transition.
     */
    public void assignInitialStatus(boolean requiresApproval) {
        if (transferStatus != null && !TransferStatus.PENDING.equals(transferStatus)) {
            throw new InvalidStatusTransitionException(statusCode(),
                    requiresApproval
                            ? TransferStatus.WAITING_FOR_APPROVAL.getCode()
                            : TransferStatus.APPROVED.getCode());
        }
        this.transferStatus = requiresApproval
                ? TransferStatus.WAITING_FOR_APPROVAL
                : TransferStatus.APPROVED;
    }

    /**
     * Assigns the initial status after creation.
     * Only PENDING transfers can be submitted to WAITING_FOR_APPROVAL or APPROVED,
     * keeping the lifecycle decision inside the Domain Model.
     */
    public void markWaitingForApproval() {
        if (transferStatus != null && !TransferStatus.PENDING.equals(transferStatus)) {
            throw new InvalidStatusTransitionException(statusCode(), TransferStatus.WAITING_FOR_APPROVAL.getCode());
        }
        this.transferStatus = TransferStatus.WAITING_FOR_APPROVAL;
    }

    public void markApproved(User approvedBy, LocalDateTime approvalDate) {
        if (!TransferStatus.WAITING_FOR_APPROVAL.equals(transferStatus)) {
            throw new InvalidStatusTransitionException(statusCode(), TransferStatus.APPROVED.getCode());
        }
        if (approvedBy == null) {
            throw new DomainException("Approving user must be provided.");
        }
        this.transferStatus = TransferStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvalDate = approvalDate == null ? LocalDateTime.now() : approvalDate;
    }

    public void markRejected() {
        if (!TransferStatus.PENDING.equals(transferStatus)
                && !TransferStatus.WAITING_FOR_APPROVAL.equals(transferStatus)) {
            throw new InvalidStatusTransitionException(statusCode(), TransferStatus.REJECTED.getCode());
        }
        this.transferStatus = TransferStatus.REJECTED;
    }

    public void markExpired() {
        if (!TransferStatus.WAITING_FOR_APPROVAL.equals(transferStatus)) {
            throw new InvalidStatusTransitionException(statusCode(), TransferStatus.EXPIRED.getCode());
        }
        this.transferStatus = TransferStatus.EXPIRED;
    }

    public void markExecuted() {
        if (!TransferStatus.APPROVED.equals(transferStatus)) {
            throw new InvalidStatusTransitionException(statusCode(), TransferStatus.EXECUTED.getCode());
        }
        this.transferStatus = TransferStatus.EXECUTED;
    }
}
