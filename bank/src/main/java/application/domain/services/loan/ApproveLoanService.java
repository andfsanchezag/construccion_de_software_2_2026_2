ackage application.domain.services.loan;

import application.domain.exceptions.CurrencyMismatchException;
import application.domain.exceptions.CustomerNotEligibleException;
import application.domain.exceptions.DestinationAccountNotActiveException;
import application.domain.exceptions.DestinationAccountOwnershipException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidApprovedAmountException;
import application.domain.exceptions.InvalidInterestRateException;
import application.domain.exceptions.InvalidLoanAmountException;
import application.domain.exceptions.InvalidLoanTermException;
import application.domain.exceptions.InvalidLoanTypeException;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.ApproveLoanUseCase;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.ports.out.LoanRepositoryPort;
import application.domain.services.authorization.AuthorizeLoanApprovalService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Approves a Loan currently under review.
 *
 * <p>Restricted to {@code INTERNAL_ANALYST} users. The state transition
 * ({@code UNDER_REVIEW ? APPROVED}) and the approval date are enforced and
 * established by the {@code Loan} Domain behavior on the authoritative persisted
 * model; the incoming loan only carries the decision data (approved amount and
 * interest rate), which belong to the Loan Domain Model.
 */
@Service
@RequiredArgsConstructor
public class ApproveLoanService implements ApproveLoanUseCase {

    private final LoanRepositoryPort loanRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final AuthorizeLoanApprovalService authorizeLoanApprovalService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public Loan approve(User user, Loan loan) {
        authorizeLoanApprovalService.execute(user);
        Loan stored = requireAuthoritativeLoan(loan);
        validateApplicant(stored);
        validateLoanType(stored);
        validateRequestedAmount(stored);
        validateTerm(stored);
        validateApprovedAmount(loan.getApprovedAmount());
        validateInterestRate(loan.getInterestRate());
        validateDestinationAccount(stored);
        String previousStatus = statusCodeOf(stored);
        stored.approve(loan.getApprovedAmount(), loan.getInterestRate());
        loanRepositoryPort.update(stored);
        registerApprovalOperation(user, stored, previousStatus);
        return stored;
    }

    private Loan requireAuthoritativeLoan(Loan loan) {
        Optional<Loan> storedOpt = loanRepositoryPort.findByIdentifier(loan);
        if (storedOpt.isEmpty()) {
            throw new EntityNotFoundException("Loan");
        }
        return storedOpt.get();
    }

    private void validateApplicant(Loan stored) {
        if (stored.getApplicant() == null) {
            throw new EntityNotFoundException("Loan applicant");
        }
        Optional<Customer> applicantOpt = customerRepositoryPort.findByIdentification(stored.getApplicant());
        if (applicantOpt.isEmpty()) {
            throw new EntityNotFoundException("Loan applicant");
        }
        Customer applicant = applicantOpt.get();
        if (!CustomerStatus.ACTIVE.equals(applicant.getStatus())) {
            throw new CustomerNotEligibleException(
                    "Customer " + applicant.getIdentification() + " is not eligible for loan approval.");
        }
    }

    private void validateLoanType(Loan stored) {
        if (stored.getLoanType() == null) {
            throw new InvalidLoanTypeException("Loan type must be provided.");
        }
    }

    private void validateRequestedAmount(Loan stored) {
        if (stored.getRequestedAmount() == null || stored.getRequestedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidLoanAmountException("Requested amount must be greater than zero.");
        }
    }

    private void validateTerm(Loan stored) {
        if (stored.getTermInMonths() == null || stored.getTermInMonths() <= 0) {
            throw new InvalidLoanTermException("Loan term must be greater than zero.");
        }
    }

    private void validateApprovedAmount(BigDecimal approvedAmount) {
        if (approvedAmount == null) {
            throw new InvalidApprovedAmountException("Approved amount must be provided.");
        }
        if (approvedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidApprovedAmountException("Approved amount must be greater than zero.");
        }
    }

    private void validateInterestRate(BigDecimal interestRate) {
        if (interestRate == null) {
            throw new InvalidInterestRateException("Interest rate must be provided.");
        }
        if (interestRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidInterestRateException("Interest rate must not be negative.");
        }
    }

    private void validateDestinationAccount(Loan stored) {
        if (stored.getDestinationAccount() == null) {
            return;
        }
        Optional<BankAccount> accountOpt = bankAccountRepositoryPort.findByIdentifier(stored.getDestinationAccount());
        if (accountOpt.isEmpty()) {
            throw new EntityNotFoundException("Destination account");
        }
        BankAccount account = accountOpt.get();
        if (!AccountStatus.ACTIVE.equals(account.getAccountStatus())) {
            throw new DestinationAccountNotActiveException("Destination account must be active.");
        }
        if (stored.getApplicant() == null
                || account.getOwner() == null
                || !stored.getApplicant().getIdentification().equals(account.getOwner().getIdentification())) {
            throw new DestinationAccountOwnershipException("Destination account must belong to the loan applicant.");
        }
        if (stored.getCurrency() == null || !stored.getCurrency().equals(account.getCurrency())) {
            throw new CurrencyMismatchException("Loan currency and destination account currency must match.");
        }
    }

    private String statusCodeOf(Loan loan) {
        return loan.getLoanStatus() == null ? "UNKNOWN" : loan.getLoanStatus().getCode();
    }

    private void registerApprovalOperation(User user, Loan loan, String previousStatus) {
        Operation op = new Operation();
        op.setOperationType(OperationType.LOAN_APPROVAL);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(loan);
        Map<String, Object> details = new HashMap<>();
        details.put("approvedAmount", loan.getApprovedAmount());
        details.put("interestRate", loan.getInterestRate());
        details.put("previousStatus", previousStatus);
        details.put("newStatus", loan.getLoanStatus().getCode());
        registerOperationAndAuditService.execute(op, details);
    }
}