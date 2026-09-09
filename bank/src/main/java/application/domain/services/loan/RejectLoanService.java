ackage application.domain.services.loan;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.RejectLoanUseCase;
import application.domain.ports.out.LoanRepositoryPort;
import application.domain.services.authorization.ValidateInternalAnalystAuthorizationService;
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
 * Rejects a Loan currently under review.
 *
 * <p>Restricted to {@code INTERNAL_ANALYST} users. The transition
 * ({@code UNDER_REVIEW ? REJECTED}) is enforced by the {@code Loan} Domain behavior
 * on the authoritative persisted model.
 */
@Service
@RequiredArgsConstructor
public class RejectLoanService implements RejectLoanUseCase {

    private final LoanRepositoryPort loanRepositoryPort;
    private final ValidateInternalAnalystAuthorizationService validateInternalAnalystAuthorizationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public Loan reject(User user, Loan loan) {
        validateRequestingUser(user);
        Loan stored = requireAuthoritativeLoan(loan);
        String previousStatus = statusCodeOf(stored);
        stored.reject();
        loanRepositoryPort.update(stored);
        registerRejectionOperation(user, stored, previousStatus);
        return stored;
    }

    private void validateRequestingUser(User user) {
        if (user == null) {
            throw new UnauthorizedOperationException("Requesting user must be provided.");
        }
        validateInternalAnalystAuthorizationService.execute(user);
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

    private void registerRejectionOperation(User user, Loan loan, String previousStatus) {
        Operation op = new Operation();
        op.setOperationType(OperationType.LOAN_REJECTION);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(loan);
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", previousStatus);
        details.put("newStatus", LoanStatus.REJECTED.getCode());
        registerOperationAndAuditService.execute(op, details);
    }
}