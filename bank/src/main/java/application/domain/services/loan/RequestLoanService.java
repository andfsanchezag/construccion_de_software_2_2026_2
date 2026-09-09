ackage application.domain.services.loan;

import application.domain.exceptions.CurrencyMismatchException;
import application.domain.exceptions.CustomerNotEligibleException;
import application.domain.exceptions.DestinationAccountNotActiveException;
import application.domain.exceptions.DestinationAccountOwnershipException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidLoanAmountException;
import application.domain.exceptions.InvalidLoanException;
import application.domain.exceptions.InvalidLoanTermException;
import application.domain.exceptions.InvalidLoanTypeException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.RequestLoanUseCase;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.ports.out.LoanRepositoryPort;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Creates a new loan application for a Customer.
 *
 * <p>Follows the standard Loan service pattern: validate User, validate Customer,
 * validate the User-Customer relationship, validate Loan data, validate the
 * destination account when applicable, and only then execute the Domain behavior
 * that establishes the initial {@code UNDER_REVIEW} state. The caller cannot control
 * the initial lifecycle state.
 */
@Service
@RequiredArgsConstructor
public class RequestLoanService implements RequestLoanUseCase {

    private final LoanRepositoryPort loanRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public Loan request(User user, Loan loan) {
        if (loan == null) {
            throw new InvalidLoanException("Loan must be provided.");
        }
        validateRequestingUser(user, loan);
        Customer applicant = requireActiveApplicant(loan);
        validateLoanData(loan);
        validateDestinationAccount(loan, applicant);
        loan.submitForReview();
        Loan saved = loanRepositoryPort.save(loan);
        registerLoanApplication(user, saved);
        return saved;
    }

    private void validateRequestingUser(User user, Loan loan) {
        if (user == null) {
            throw new UnauthorizedOperationException("Requesting user must be provided.");
        }
        validateUserAuthorizationStatusService.execute(user);
        if (isEmployee(user)) {
            return;
        }
        if (user.getCustomer() == null
                || loan.getApplicant() == null
                || !user.getCustomer().getIdentification().equals(loan.getApplicant().getIdentification())) {
            throw new UnauthorizedOperationException(
                    "User is not authorized to request a loan for the given applicant.");
        }
    }

    private Customer requireActiveApplicant(Loan loan) {
        Optional<Customer> applicantOpt = customerRepositoryPort.findByIdentification(loan.getApplicant());
        if (applicantOpt.isEmpty()) {
            throw new EntityNotFoundException("Loan applicant");
        }
        Customer applicant = applicantOpt.get();
        if (!CustomerStatus.ACTIVE.equals(applicant.getStatus())) {
            throw new CustomerNotEligibleException(
                    "Customer " + applicant.getIdentification()
                            + " is not eligible for a new loan application.");
        }
        return applicant;
    }

    private void validateLoanData(Loan loan) {
        if (loan.getLoanType() == null) {
            throw new InvalidLoanTypeException("Loan type must be provided.");
        }
        if (loan.getRequestedAmount() == null || loan.getRequestedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidLoanAmountException("Requested amount must be greater than zero.");
        }
        if (loan.getTermInMonths() == null || loan.getTermInMonths() <= 0) {
            throw new InvalidLoanTermException("Loan term must be greater than zero.");
        }
        if (loan.getCurrency() == null) {
            throw new InvalidLoanException("Loan currency must be provided.");
        }
    }

    private void validateDestinationAccount(Loan loan, Customer applicant) {
        if (loan.getDestinationAccount() == null) {
            return;
        }
        Optional<BankAccount> accountOpt = bankAccountRepositoryPort.findByIdentifier(loan.getDestinationAccount());
        if (accountOpt.isEmpty()) {
            throw new EntityNotFoundException("Destination account");
        }
        BankAccount account = accountOpt.get();
        if (!AccountStatus.ACTIVE.equals(account.getAccountStatus())) {
            throw new DestinationAccountNotActiveException("Destination account must be active.");
        }
        if (account.getOwner() == null
                || !applicant.getIdentification().equals(account.getOwner().getIdentification())) {
            throw new DestinationAccountOwnershipException("Destination account must belong to the loan applicant.");
        }
        if (!sameCurrency(loan, account)) {
            throw new CurrencyMismatchException("Loan currency and destination account currency must match.");
        }
    }

    private boolean sameCurrency(Loan loan, BankAccount account) {
        return loan.getCurrency() != null && loan.getCurrency().equals(account.getCurrency());
    }

    private boolean isEmployee(User user) {
        return SystemRole.TELLER_EMPLOYEE.equals(user.getRole())
                || SystemRole.COMMERCIAL_EMPLOYEE.equals(user.getRole())
                || SystemRole.INTERNAL_ANALYST.equals(user.getRole());
    }

    private void registerLoanApplication(User user, Loan loan) {
        Operation op = new Operation();
        op.setOperationType(OperationType.LOAN_APPLICATION);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(loan);
        Map<String, Object> details = new HashMap<>();
        details.put("requestedAmount", loan.getRequestedAmount());
        details.put("loanType", loan.getLoanType().getCode());
        details.put("currency", loan.getCurrency().getCode());
        registerOperationAndAuditService.execute(op, details);
    }
}