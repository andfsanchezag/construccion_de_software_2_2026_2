ackage application.domain.services.loan;

import application.domain.enums.ApprovalDecision;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidLoanStatusException;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.Loan;
import application.domain.ports.in.EvaluateLoanUseCase;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.ports.out.LoanRepositoryPort;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.LoanStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Evaluates whether a Loan satisfies the Domain conditions to proceed toward
 * approval or rejection, without mutating the Loan.
 *
 * <p>Evaluation is conceptually separated from the state-changing approval/rejection
 * operations and communicates the outcome as an {@link ApprovalDecision}.
 */
@Service
@RequiredArgsConstructor
public class EvaluateLoanService implements EvaluateLoanUseCase {

    private final LoanRepositoryPort loanRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;

    @Override
    public ApprovalDecision evaluate(Loan loan) {
        Loan stored = requireAuthoritativeLoan(loan);
        if (!LoanStatus.UNDER_REVIEW.equals(stored.getLoanStatus())) {
            throw new InvalidLoanStatusException(
                    "Loan with status " + statusCodeOf(stored) + " cannot be evaluated as a new application.");
        }
        if (!isApplicantEligible(stored)) {
            return ApprovalDecision.REJECTED;
        }
        if (!hasValidLoanData(stored)) {
            return ApprovalDecision.REJECTED;
        }
        if (stored.getDestinationAccount() != null && !isDestinationAccountValid(stored)) {
            return ApprovalDecision.REJECTED;
        }
        return ApprovalDecision.APPROVED;
    }

    private Loan requireAuthoritativeLoan(Loan loan) {
        Optional<Loan> storedOpt = loanRepositoryPort.findByIdentifier(loan);
        if (storedOpt.isEmpty()) {
            throw new EntityNotFoundException("Loan");
        }
        return storedOpt.get();
    }

    private boolean isApplicantEligible(Loan stored) {
        if (stored.getApplicant() == null) {
            return false;
        }
        Optional<Customer> applicantOpt = customerRepositoryPort.findByIdentification(stored.getApplicant());
        if (applicantOpt.isEmpty()) {
            return false;
        }
        return CustomerStatus.ACTIVE.equals(applicantOpt.get().getStatus());
    }

    private boolean hasValidLoanData(Loan stored) {
        if (stored.getLoanType() == null || stored.getCurrency() == null) {
            return false;
        }
        if (stored.getRequestedAmount() == null || stored.getRequestedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (stored.getTermInMonths() == null || stored.getTermInMonths() <= 0) {
            return false;
        }
        return stored.getInterestRate() == null || stored.getInterestRate().compareTo(BigDecimal.ZERO) >= 0;
    }

    private boolean isDestinationAccountValid(Loan stored) {
        Optional<BankAccount> accountOpt = bankAccountRepositoryPort.findByIdentifier(stored.getDestinationAccount());
        if (accountOpt.isEmpty()) {
            return false;
        }
        BankAccount account = accountOpt.get();
        if (!AccountStatus.ACTIVE.equals(account.getAccountStatus())) {
            return false;
        }
        if (stored.getApplicant() == null
                || account.getOwner() == null
                || !stored.getApplicant().getIdentification().equals(account.getOwner().getIdentification())) {
            return false;
        }
        return stored.getCurrency() != null && stored.getCurrency().equals(account.getCurrency());
    }

    private String statusCodeOf(Loan loan) {
        return loan.getLoanStatus() == null ? "UNKNOWN" : loan.getLoanStatus().getCode();
    }
}