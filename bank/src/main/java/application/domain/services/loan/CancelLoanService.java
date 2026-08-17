package application.domain.services.loan;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.out.LoanRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.LoanStatus;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CancelLoanService {

    private static final Set<LoanStatus> CANCELLABLE_STATUSES = Set.of(
            LoanStatus.UNDER_REVIEW, LoanStatus.APPROVED, LoanStatus.OVERDUE
    );

    private final LoanRepositoryPort loanRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Loan execute(User user, Loan loan) {
        Loan stored = loanRepositoryPort.findByIdentifier(loan)
                .orElseThrow(() -> new EntityNotFoundException("Loan"));
        if (!CANCELLABLE_STATUSES.contains(stored.getLoanStatus())) {
            throw new DomainException("Loan with status " + stored.getLoanStatus().getCode() + " cannot be cancelled.");
        }
        String previousStatus = stored.getLoanStatus().getCode();
        stored.setLoanStatus(LoanStatus.CANCELLED);
        loanRepositoryPort.update(stored);
        Operation op = new Operation();
        op.setOperationType(OperationType.LOAN_CANCELLATION);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(stored);
        registerOperationAndAuditService.execute(op, Map.of(
                "previousStatus", previousStatus,
                "newStatus", LoanStatus.CANCELLED.getCode()
        ));
        return stored;
    }
}
