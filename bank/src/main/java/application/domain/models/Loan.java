package application.domain.models;

import application.domain.exceptions.InvalidApprovedAmountException;
import application.domain.exceptions.InvalidInterestRateException;
import application.domain.exceptions.InvalidLoanAmountException;
import application.domain.exceptions.InvalidLoanException;
import application.domain.exceptions.InvalidLoanStatusTransitionException;
import application.domain.exceptions.InvalidLoanTermException;
import application.domain.exceptions.InvalidLoanTypeException;
import application.domain.valueobjects.Currency;
import application.domain.valueobjects.LoanStatus;
import application.domain.valueobjects.LoanType;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Domain Model for a Loan banking product.
 *
 * <p>The lifecycle of a Loan is owned by this model: callers must not arbitrarily
 * assign {@code loanStatus}, {@code approvalDate} or {@code disbursementDate}.
 * Every state change goes through the corresponding Domain behavior
 * (submitForReview / approve / reject / disburse / cancel / markOverdue), which is
 * the only authority for valid transitions (see under-review → approved → disbursed
 * → overdue and the cancellation rules defined by the Loan services specification).
 *
 * <p>Monetary values remain {@link BigDecimal}. {@code BankingProduct} does not expose
 * a currency, so the Loan explicitly represents its own {@link Currency}.
 */
@Getter
@Setter
@NoArgsConstructor
public class Loan extends BankingProduct {
    private Customer applicant;
    private LoanType loanType;
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private BigDecimal interestRate;
    private Integer termInMonths;
    @Setter(AccessLevel.NONE)
    private LoanStatus loanStatus;
    @Setter(AccessLevel.NONE)
    private LocalDate approvalDate;
    @Setter(AccessLevel.NONE)
    private LocalDate disbursementDate;
    private BankAccount destinationAccount;
    private Currency currency;

    /**
     * Enters the loan into the {@code UNDER_REVIEW} lifecycle state.
     *
     * <p>Serves as the initial Domain transition for new applications. The caller
     * must never be able to create an already-approved loan, so a Loan that already
     * has a lifecycle state cannot be submitted for review again.
     */
    public void submitForReview() {
        if (loanStatus != null) {
            throw new InvalidLoanStatusTransitionException(statusCode(), LoanStatus.UNDER_REVIEW.getCode());
        }
        if (applicant == null) {
            throw new InvalidLoanException("Loan applicant must be provided.");
        }
        if (loanType == null) {
            throw new InvalidLoanTypeException("Loan type must be provided.");
        }
        if (requestedAmount == null || requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidLoanAmountException("Requested amount must be greater than zero.");
        }
        if (termInMonths == null || termInMonths <= 0) {
            throw new InvalidLoanTermException("Loan term must be greater than zero.");
        }
        if (currency == null) {
            throw new InvalidLoanException("Loan currency must be provided.");
        }
        this.loanStatus = LoanStatus.UNDER_REVIEW;
    }

    /**
     * Approves the loan: {@code UNDER_REVIEW → APPROVED}.
     *
     * <p>The approval date is established by this transition; the caller cannot
     * supply or override it. The approved amount and interest rate belong to the
     * Loan Domain Model and must satisfy the Domain invariants.
     */
    public void approve(BigDecimal approvedAmount, BigDecimal interestRate) {
        if (!LoanStatus.UNDER_REVIEW.equals(loanStatus)) {
            throw new InvalidLoanStatusTransitionException(statusCode(), LoanStatus.APPROVED.getCode());
        }
        if (approvedAmount == null) {
            throw new InvalidApprovedAmountException("Approved amount must be provided.");
        }
        if (approvedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidApprovedAmountException("Approved amount must be greater than zero.");
        }
        if (requestedAmount != null && approvedAmount.compareTo(requestedAmount) > 0) {
            throw new InvalidApprovedAmountException("Approved amount cannot be greater than the requested amount.");
        }
        if (interestRate == null) {
            throw new InvalidInterestRateException("Interest rate must be provided.");
        }
        if (interestRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInterestRateException("Interest rate must not be negative.");
        }
        this.approvedAmount = approvedAmount;
        this.interestRate = interestRate;
        this.loanStatus = LoanStatus.APPROVED;
        this.approvalDate = LocalDate.now();
    }

    /**
     * Rejects the loan: {@code UNDER_REVIEW → REJECTED}.
     */
    public void reject() {
        if (!LoanStatus.UNDER_REVIEW.equals(loanStatus)) {
            throw new InvalidLoanStatusTransitionException(statusCode(), LoanStatus.REJECTED.getCode());
        }
        this.loanStatus = LoanStatus.REJECTED;
    }
/**
     * Disburses the approved amount: {@code APPROVED → DISBURSED}.
     *
     * <p>The disbursement date is established by this transition; the caller cannot
     * supply or override it. The actual crediting of the destination account is a
     * separate BankAccount Domain behavior.
     */
    public void disburse() {
        if (!LoanStatus.APPROVED.equals(loanStatus)) {
            throw new InvalidLoanStatusTransitionException(statusCode(), LoanStatus.DISBURSED.getCode());
        }
        if (approvedAmount == null || approvedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidApprovedAmountException("Loan cannot be disbursed without a positive approved amount.");
        }
        this.loanStatus = LoanStatus.DISBURSED;
        this.disbursementDate = LocalDate.now();
    }

    /**
     * Marks the loan overdue: {@code DISBURSED → OVERDUE}.
     *
     * <p>Automatic overdue detection may be driven by an application/scheduled
     * mechanism; the Domain remains the authority that decides whether the
     * transition is valid.
     */
    public void markOverdue() {
        if (!LoanStatus.DISBURSED.equals(loanStatus)) {
            throw new InvalidLoanStatusTransitionException(statusCode(), LoanStatus.OVERDUE.getCode());
        }
        this.loanStatus = LoanStatus.OVERDUE;
    }

    /**
     * Cancels the loan when its current state allows it:
     * {@code UNDER_REVIEW} / {@code APPROVED} / {@code OVERDUE} → {@code CANCELLED}.
     *
     * <p>Cancellation from {@code REJECTED} or {@code DISBURSED} is not permitted by
     * the current lifecycle. Outstanding-obligation settlement rules that may be
     * required for overdue cancellations must be defined by future Domain policies
     * once the obligation model exists.
     */
    public void cancel() {
        if (!canBeCancelled()) {
            throw new InvalidLoanStatusTransitionException(statusCode(), LoanStatus.CANCELLED.getCode());
        }
        this.loanStatus = LoanStatus.CANCELLED;
    }

    public boolean isApproved() {
        return LoanStatus.APPROVED.equals(loanStatus);
    }

    public boolean canBeDisbursed() {
        return LoanStatus.APPROVED.equals(loanStatus);
    }

    public boolean canBeCancelled() {
        return LoanStatus.UNDER_REVIEW.equals(loanStatus)
                || LoanStatus.APPROVED.equals(loanStatus)
                || LoanStatus.OVERDUE.equals(loanStatus);
    }

    private String statusCode() {
        return loanStatus == null ? "UNKNOWN" : loanStatus.getCode();
    }
}
