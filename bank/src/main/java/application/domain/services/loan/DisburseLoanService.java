ackage application.domain.services.loan;

import application.domain.exceptions.CurrencyMismatchException;
import application.domain.exceptions.DestinationAccountNotActiveException;
import application.domain.exceptions.DestinationAccountOwnershipException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidApprovedAmountException;
import application.domain.exceptions.InvalidDestinationAccountException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.BankAccount;
import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.DisburseLoanUseCase;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.ports.out.LoanRepositoryPort;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Disburses the approved Loan amount to the destination BankAccount.
 *
 * <p>This is a state-changing financial operation: the account is credited through
 * the BankAccount Domain behavior ({@code deposit}), the Loan transitions
 * ({@code APPROVED ? DISBURSED}) and sets its own disbursement date, and the
 * Operation/AuditLog are registered. The account must be ACTIVE, must belong to the
 * applicant, and its currency must match the Loan currency.
 */
@Service
@RequiredArgsConstructor
public class DisburseLoanService implements DisburseLoanUseCase {

    private final LoanRepositoryPort loanRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public Loan disburse(User user, Loan loan) {
        validateRequestingUser(user);
        Loan stored = requireAuthoritativeLoan(loan);
        validateApprovedAmount(stored);
        BankAccount destination = requireDestinationAccount(stored);
        validateDestinationAccount(stored, destination);
        destination.deposit(Money.of(stored.getApprovedAmount(), stored.getCurrency()));
        bankAccountRepositoryPort.update(destination);
        stored.disburse();
        loanRepositoryPort.update(stored);
        registerDisbursementOperation(user, stored, destination);
        return stored;
    }

    private void validateRequestingUser(User user) {
        if (user == null) {
            throw new UnauthorizedOperationException("Requesting user must be provided.");
        }
        validateUserAuthorizationStatusService.execute(user);
    }

    private Loan requireAuthoritativeLoan(Loan loan) {
        Optional<Loan> storedOpt = loanRepositoryPort.findByIdentifier(loan);
        if (storedOpt.isEmpty()) {
            throw new EntityNotFoundException("Loan");
        }
        return storedOpt.get();
    }

    private void validateApprovedAmount(Loan stored) {
        if (stored.getApprovedAmount() == null || stored.getApprovedAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new InvalidApprovedAmountException("Loan cannot be disbursed without a positive approved amount.");
        }
    }

    private BankAccount requireDestinationAccount(Loan stored) {
        if (stored.getDestinationAccount() == null) {
            throw new InvalidDestinationAccountException("Destination account must be provided for disbursement.");
        }
        Optional<BankAccount> destinationOpt = bankAccountRepositoryPort.findByIdentifier(stored.getDestinationAccount());
        if (destinationOpt.isEmpty()) {
            throw new EntityNotFoundException("Destination account");
        }
        return destinationOpt.get();
    }

    private void validateDestinationAccount(Loan stored, BankAccount destination) {
        if (!AccountStatus.ACTIVE.equals(destination.getAccountStatus())) {
            throw new DestinationAccountNotActiveException(
                    "Destination account must be active for disbursement.");
        }
        if (stored.getApplicant() == null
                || destination.getOwner() == null
                || !stored.getApplicant().getIdentification().equals(destination.getOwner().getIdentification())) {
            throw new DestinationAccountOwnershipException("Destination account must belong to the loan applicant.");
        }
        if (stored.getCurrency() == null || !stored.getCurrency().equals(destination.getCurrency())) {
            throw new CurrencyMismatchException("Loan currency and destination account currency must match.");
        }
    }

    private void registerDisbursementOperation(User user, Loan loan, BankAccount destination) {
        Operation op = new Operation();
        op.setOperationType(OperationType.LOAN_DISBURSEMENT);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(loan);
        Map<String, Object> details = new HashMap<>();
        details.put("approvedAmount", loan.getApprovedAmount());
        details.put("destinationAccount", destination.getIdentifier());
        details.put("disbursementDate", loan.getDisbursementDate());
        registerOperationAndAuditService.execute(op, details);
    }
}