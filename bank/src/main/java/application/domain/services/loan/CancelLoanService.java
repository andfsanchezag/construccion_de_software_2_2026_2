ackage application.domain.services.loan;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.CancelLoanUseCase;
import application.domain.ports.out.LoanRepositoryPort;
import application.domain.services.authorization.AuthorizeLoanOperationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.LoanStatus;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Cancels a Loan when its current state allows cancellation.
 *
 * <p>Authorization depends on the Loan state, UserStatus and SystemRole; it is not
 * delegated to the controller. The transition
 * ({@code UNDER_REVIEW} / {@code APPROVED} / {@code OVERDUE} ? {@code CANCELLED}) is
 * enforced by the {@code Loan} Domain behavior.
 *
 * <p>Outstanding-obligation settlement rules that may apply to overdue cancellations
 * are not yet defined by the Domain (the obligation model does not exist); they must
 * be added through explicit Domain policies when introduced.
 */
@Service
@RequiredArgsConstructor
public class CancelLoanService implements CancelLoanUseCase {

    private final LoanRepositoryPort loanRepositoryPort;
    private final AuthorizeLoanOperationService authorizeLoanOperationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public Loan cancel(User user, Loan loan) {
        Loan stored = requireAuthoritativeLoan(loan);
        authorizeLoanOperationService.execute(user, stored);
        String previousStatus = statusCodeOf(stored);
        stored.cancel();
        loanRepositoryPort.update(stored);
        registerCancellationOperation(user, stored, previousStatus);
        return stored;
    }

    private Loan requireAuthoritativeLoan(Loan loan) {
        Optional<Loan> storedOpt = loanRepositoryPort.findByIdentifier(loan);
        if (storedOpt.isEmpty()) {
            throw new EntityNotFoundException("Loan");
        }
        return storedOpt.get();
    }

    private String statusCodeOf(Loan loan) {
        return loan.getLoanStatus() == null ? "UNKNOWN" : loan.getLoanStatus().getCode();
    }

    private void registerCancellationOperation(User user, Loan loan, String previousStatus) {
        Operation op = new Operation();
        op.setOperationType(OperationType.LOAN_CANCELLATION);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(loan);
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", previousStatus);
        details.put("newStatus", LoanStatus.CANCELLED.getCode());
        registerOperationAndAuditService.execute(op, details);
    }
}