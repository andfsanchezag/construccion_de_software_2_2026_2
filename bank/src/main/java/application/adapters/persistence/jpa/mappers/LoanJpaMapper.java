package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.LoanJpaEntity;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.Loan;
import application.domain.valueobjects.LoanStatus;
import application.domain.valueobjects.LoanType;
import java.lang.reflect.Field;

/**
 * Bidirectional mapper between the Loan Domain Model and its JPA entity.
 *
 * <p>The Loan Domain Model deliberately blocks direct assignment of its lifecycle
 * fields ({@code loanStatus}, {@code approvalDate}, {@code disbursementDate}) to keep
 * the transitions authoritative. A persistence mapper must restore the previously
 * persisted state without replaying the transitions (which would reset the dates to
 * "now"), so those three fields are restored reflectively. Everything else uses the
 * public accessors.
 */
public final class LoanJpaMapper {

    private LoanJpaMapper() {
    }

    public static LoanJpaEntity toEntity(Loan domain) {
        if (domain == null) {
            return null;
        }
        LoanJpaEntity entity = new LoanJpaEntity();
        entity.setIdentifier(domain.getIdentifier());
        entity.setApplicantIdentification(
                domain.getApplicant() != null ? domain.getApplicant().getIdentification() : null);
        entity.setLoanType(domain.getLoanType() != null ? domain.getLoanType().getCode() : null);
        entity.setRequestedAmount(domain.getRequestedAmount());
        entity.setApprovedAmount(domain.getApprovedAmount());
        entity.setInterestRate(domain.getInterestRate());
        entity.setTermInMonths(domain.getTermInMonths());
        entity.setLoanStatus(domain.getLoanStatus() != null ? domain.getLoanStatus().getCode() : null);
        entity.setApprovalDate(domain.getApprovalDate());
        entity.setDisbursementDate(domain.getDisbursementDate());
        entity.setDestinationAccountIdentifier(
                domain.getDestinationAccount() != null ? domain.getDestinationAccount().getIdentifier() : null);
        entity.setCurrency(domain.getCurrency() != null ? domain.getCurrency().getCode() : null);
        return entity;
    }

    public static Loan toDomain(LoanJpaEntity entity, Customer applicant, BankAccount destinationAccount) {
        if (entity == null) {
            return null;
        }
        Loan domain = new Loan();
        domain.setIdentifier(entity.getIdentifier());
        domain.setApplicant(applicant);
        domain.setLoanType(loanType(entity.getLoanType()));
        domain.setRequestedAmount(entity.getRequestedAmount());
        domain.setApprovedAmount(entity.getApprovedAmount());
        domain.setInterestRate(entity.getInterestRate());
        domain.setTermInMonths(entity.getTermInMonths());
        restoreLifecycleField(domain, "loanStatus", loanStatus(entity.getLoanStatus()));
        restoreLifecycleField(domain, "approvalDate", entity.getApprovalDate());
        restoreLifecycleField(domain, "disbursementDate", entity.getDisbursementDate());
        domain.setDestinationAccount(destinationAccount);
        domain.setCurrency(BankAccountJpaMapper.currency(entity.getCurrency()));
        return domain;
    }

    private static void restoreLifecycleField(Loan loan, String fieldName, Object value) {
        try {
            Field field = Loan.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(loan, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to restore Loan lifecycle field '" + fieldName + "'.", exception);
        }
    }

    public static LoanType loanType(String code) {
        if (code == null) {
            return null;
        }
        if (LoanType.PERSONAL.getCode().equals(code)) {
            return LoanType.PERSONAL;
        }
        if (LoanType.MORTGAGE.getCode().equals(code)) {
            return LoanType.MORTGAGE;
        }
        if (LoanType.VEHICLE.getCode().equals(code)) {
            return LoanType.VEHICLE;
        }
        if (LoanType.BUSINESS.getCode().equals(code)) {
            return LoanType.BUSINESS;
        }
        return null;
    }

    public static LoanStatus loanStatus(String code) {
        if (code == null) {
            return null;
        }
        if (LoanStatus.UNDER_REVIEW.getCode().equals(code)) {
            return LoanStatus.UNDER_REVIEW;
        }
        if (LoanStatus.APPROVED.getCode().equals(code)) {
            return LoanStatus.APPROVED;
        }
        if (LoanStatus.REJECTED.getCode().equals(code)) {
            return LoanStatus.REJECTED;
        }
        if (LoanStatus.DISBURSED.getCode().equals(code)) {
            return LoanStatus.DISBURSED;
        }
        if (LoanStatus.OVERDUE.getCode().equals(code)) {
            return LoanStatus.OVERDUE;
        }
        if (LoanStatus.CANCELLED.getCode().equals(code)) {
            return LoanStatus.CANCELLED;
        }
        return null;
    }
}
