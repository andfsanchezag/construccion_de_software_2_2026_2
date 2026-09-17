package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.TransferJpaEntity;
import application.domain.models.BankAccount;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.valueobjects.TransferStatus;

/**
 * Bidirectional mapper between the Transfer Domain Model and its JPA entity.
 *
 * <p>The referenced accounts and actors are stored as identifiers. The adapter resolves
 * the corresponding Domain Models and supplies them to {@link #toDomain}.
 */
public final class TransferJpaMapper {

    private TransferJpaMapper() {
    }

    public static TransferJpaEntity toEntity(Transfer domain) {
        if (domain == null) {
            return null;
        }
        TransferJpaEntity entity = new TransferJpaEntity();
        entity.setIdentifier(domain.getIdentifier());
        entity.setSourceAccountIdentifier(
                domain.getSourceAccount() != null ? domain.getSourceAccount().getIdentifier() : null);
        entity.setDestinationAccountIdentifier(
                domain.getDestinationAccount() != null ? domain.getDestinationAccount().getIdentifier() : null);
        entity.setAmount(domain.getAmount());
        entity.setCreationDate(domain.getCreationDate());
        entity.setApprovalDate(domain.getApprovalDate());
        entity.setTransferStatus(domain.getTransferStatus() != null ? domain.getTransferStatus().getCode() : null);
        entity.setCreatedByUserId(domain.getCreatedBy() != null ? domain.getCreatedBy().getUserId() : null);
        entity.setApprovedByUserId(domain.getApprovedBy() != null ? domain.getApprovedBy().getUserId() : null);
        return entity;
    }

    public static Transfer toDomain(TransferJpaEntity entity, BankAccount sourceAccount,
                                    BankAccount destinationAccount, User createdBy, User approvedBy) {
        if (entity == null) {
            return null;
        }
        Transfer domain = new Transfer();
        domain.setIdentifier(entity.getIdentifier());
        domain.setSourceAccount(sourceAccount);
        domain.setDestinationAccount(destinationAccount);
        domain.setAmount(entity.getAmount());
        domain.setCreationDate(entity.getCreationDate());
        domain.setApprovalDate(entity.getApprovalDate());
        domain.setTransferStatus(transferStatus(entity.getTransferStatus()));
        domain.setCreatedBy(createdBy);
        domain.setApprovedBy(approvedBy);
        return domain;
    }

    public static TransferStatus transferStatus(String code) {
        if (code == null) {
            return null;
        }
        if (TransferStatus.PENDING.getCode().equals(code)) {
            return TransferStatus.PENDING;
        }
        if (TransferStatus.WAITING_FOR_APPROVAL.getCode().equals(code)) {
            return TransferStatus.WAITING_FOR_APPROVAL;
        }
        if (TransferStatus.APPROVED.getCode().equals(code)) {
            return TransferStatus.APPROVED;
        }
        if (TransferStatus.EXECUTED.getCode().equals(code)) {
            return TransferStatus.EXECUTED;
        }
        if (TransferStatus.REJECTED.getCode().equals(code)) {
            return TransferStatus.REJECTED;
        }
        if (TransferStatus.EXPIRED.getCode().equals(code)) {
            return TransferStatus.EXPIRED;
        }
        return null;
    }
}
