package application.adapters.persistence.mongodb.mappers;

import application.adapters.persistence.mongodb.documents.AuditLogDocument;
import application.domain.models.AuditLog;
import application.domain.models.BankAccount;
import application.domain.models.BankingProduct;
import application.domain.models.Loan;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.SystemRole;

/**
 * Bidirectional mapper between the AuditLog Domain Model and its MongoDB document.
 *
 * <p>The acting user and the affected product are reference aggregates in the domain;
 * they are stored denormalized (identifier plus the display attributes the audit trail
 * needs) so reading an audit entry never requires loading other aggregates.
 */
public final class AuditLogMongoMapper {

    public static final String PRODUCT_BANK_ACCOUNT = "BANK_ACCOUNT";
    public static final String PRODUCT_LOAN = "LOAN";
    public static final String PRODUCT_TRANSFER = "TRANSFER";

    private AuditLogMongoMapper() {
    }

    public static AuditLogDocument toDocument(AuditLog domain) {
        if (domain == null) {
            return null;
        }
        AuditLogDocument document = new AuditLogDocument();
        document.setAuditId(domain.getAuditId());
        document.setOperationType(domain.getOperationType() != null ? domain.getOperationType().getCode() : null);
        document.setOperationDate(domain.getOperationDate());
        User performedBy = domain.getPerformedBy();
        if (performedBy != null) {
            document.setPerformedByUserId(performedBy.getUserId());
            document.setPerformedByUsername(performedBy.getUsername());
            document.setPerformedByRole(performedBy.getRole() != null ? performedBy.getRole().getCode() : null);
        }
        document.setUserRole(domain.getUserRole() != null ? domain.getUserRole().getCode() : null);
        document.setAffectedProductIdentifier(productIdentifier(domain.getAffectedProduct()));
        document.setAffectedProductType(productType(domain.getAffectedProduct()));
        document.setDetails(domain.getDetails());
        return document;
    }

    public static AuditLog toDomain(AuditLogDocument document) {
        if (document == null) {
            return null;
        }
        AuditLog domain = new AuditLog();
        domain.setAuditId(document.getAuditId());
        domain.setOperationType(operationType(document.getOperationType()));
        domain.setOperationDate(document.getOperationDate());
        domain.setPerformedBy(userReference(document));
        domain.setUserRole(systemRole(document.getUserRole()));
        domain.setAffectedProduct(productReference(document.getAffectedProductType(),
                document.getAffectedProductIdentifier()));
        domain.setDetails(document.getDetails());
        return domain;
    }

    private static User userReference(AuditLogDocument document) {
        if (document.getPerformedByUserId() == null
                && document.getPerformedByUsername() == null
                && document.getPerformedByRole() == null) {
            return null;
        }
        User user = new User();
        user.setUserId(document.getPerformedByUserId());
        user.setUsername(document.getPerformedByUsername());
        user.setRole(systemRole(document.getPerformedByRole()));
        return user;
    }

    /**
     * Rebuilds a typed product reference from the denormalized discriminator and
     * identifier stored in the audit document.
     */
    private static BankingProduct productReference(String type, String identifier) {
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

    private static String productIdentifier(BankingProduct product) {
        return product != null ? product.getIdentifier() : null;
    }

    private static String productType(BankingProduct product) {
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

    public static SystemRole systemRole(String code) {
        if (code == null) {
            return null;
        }
        SystemRole[] knownRoles = {
            SystemRole.NATURAL_CUSTOMER,
            SystemRole.BUSINESS_CUSTOMER,
            SystemRole.TELLER_EMPLOYEE,
            SystemRole.COMMERCIAL_EMPLOYEE,
            SystemRole.BUSINESS_OPERATOR,
            SystemRole.BUSINESS_SUPERVISOR,
            SystemRole.INTERNAL_ANALYST
        };
        for (SystemRole candidate : knownRoles) {
            if (candidate.getCode().equals(code)) {
                return candidate;
            }
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
