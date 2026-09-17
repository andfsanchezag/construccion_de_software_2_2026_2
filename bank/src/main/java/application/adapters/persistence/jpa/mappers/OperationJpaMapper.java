package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.OperationJpaEntity;
import application.domain.models.BankAccount;
import application.domain.models.BankingProduct;
import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.valueobjects.OperationType;

/**
 * Bidirectional mapper between the Operation Domain Model and its JPA entity.
 *
 * <p>{@code Operation.affectedProduct} is a polymorphic {@link BankingProduct}
 * reference. It is stored as an identifier plus a product type discriminator, so the
 * matching Domain Model type can be rebuilt when reading.
 */
public final class OperationJpaMapper {

    public static final String PRODUCT_BANK_ACCOUNT = "BANK_ACCOUNT";
    public static final String PRODUCT_LOAN = "LOAN";
    public static final String PRODUCT_TRANSFER = "TRANSFER";

    private OperationJpaMapper() {
    }

    public static OperationJpaEntity toEntity(Operation domain) {
        if (domain == null) {
            return null;
        }
        OperationJpaEntity entity = new OperationJpaEntity();
        entity.setOperationId(domain.getOperationId());
        entity.setOperationType(domain.getOperationType() != null ? domain.getOperationType().getCode() : null);
        entity.setExecutionDate(domain.getExecutionDate());
        entity.setPerformedByUserId(domain.getPerformedBy() != null ? domain.getPerformedBy().getUserId() : null);
        entity.setAffectedProductIdentifier(productIdentifier(domain.getAffectedProduct()));
        entity.setAffectedProductType(productType(domain.getAffectedProduct()));
        return entity;
    }

    public static Operation toDomain(OperationJpaEntity entity, User performedBy, BankingProduct affectedProduct) {
        if (entity == null) {
            return null;
        }
        Operation domain = new Operation();
        domain.setOperationId(entity.getOperationId());
        domain.setOperationType(operationType(entity.getOperationType()));
        domain.setExecutionDate(entity.getExecutionDate());
        domain.setPerformedBy(performedBy);
        domain.setAffectedProduct(affectedProduct);
        return domain;
    }

    /**
     * Rebuilds a typed product reference from the persisted discriminator and identifier.
     * Only the identity of the referenced aggregate is restored, which is enough for the
     * operation history (query filters always use the persisted identifier column).
     */
    public static BankingProduct toProductReference(String type, String identifier) {
        if (identifier == null) {
            return null;
        }
        if (PRODUCT_LOAN.equals(type)) {
            Loan loan = new Loan();
            loan.setIdentifier(identifier);
            return loan;
        }
        if (PRODUCT_TRANSFER.equals(type)) {
            Transfer transfer = new Transfer();
            transfer.setIdentifier(identifier);
            return transfer;
        }
        if (PRODUCT_BANK_ACCOUNT.equals(type)) {
            BankAccount account = new BankAccount();
            account.setIdentifier(identifier);
            return account;
        }
        return null;
    }

    public static String productIdentifier(BankingProduct product) {
        return product != null ? product.getIdentifier() : null;
    }

    public static String productType(BankingProduct product) {
        if (product instanceof Loan) {
            return PRODUCT_LOAN;
        }
        if (product instanceof Transfer) {
            return PRODUCT_TRANSFER;
        }
        if (product instanceof BankAccount) {
            return PRODUCT_BANK_ACCOUNT;
        }
        return null;
    }

    public static OperationType operationType(String code) {
        if (code == null) {
            return null;
        }
        OperationType[] knownTypes = {
            OperationType.ACCOUNT_OPENING,
            OperationType.DEPOSIT,
            OperationType.WITHDRAWAL,
            OperationType.ACCOUNT_BLOCKING,
            OperationType.ACCOUNT_UNBLOCKING,
            OperationType.ACCOUNT_CLOSING,
            OperationType.TRANSFER_CREATION,
            OperationType.TRANSFER_APPROVAL,
            OperationType.TRANSFER_REJECTION,
            OperationType.TRANSFER_EXECUTION,
            OperationType.TRANSFER_EXPIRATION,
            OperationType.LOAN_APPLICATION,
            OperationType.LOAN_APPROVAL,
            OperationType.LOAN_REJECTION,
            OperationType.LOAN_DISBURSEMENT,
            OperationType.LOAN_PAYMENT,
            OperationType.LOAN_OVERDUE,
            OperationType.LOAN_CANCELLATION,
            OperationType.CUSTOMER_REGISTRATION,
            OperationType.CUSTOMER_UPDATE,
            OperationType.CUSTOMER_STATUS_CHANGE
        };
        for (OperationType candidate : knownTypes) {
            if (candidate.getCode().equals(code)) {
                return candidate;
            }
        }
        return null;
    }
}
