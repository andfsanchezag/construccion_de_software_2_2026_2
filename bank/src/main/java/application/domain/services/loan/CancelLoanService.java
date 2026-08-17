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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CancelLoanService {

    private final LoanRepositoryPort loanRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Loan execute(User user, Loan loan) {
        Optional<Loan> storedOpt = loanRepositoryPort.findByIdentifier(loan);
        if (storedOpt.isEmpty()) {
            throw new EntityNotFoundException("Loan");
        }
        Loan stored = storedOpt.get();
        LoanStatus currentStatus = stored.getLoanStatus();
        if (!LoanStatus.UNDER_REVIEW.equals(currentStatus)
                && !LoanStatus.APPROVED.equals(currentStatus)
                && !LoanStatus.OVERDUE.equals(currentStatus)) {
            throw new DomainException("Loan with status " + currentStatus.getCode() + " cannot be cancelled.");
        }
        String previousStatus = currentStatus.getCode();
        stored.setLoanStatus(LoanStatus.CANCELLED);
        loanRepositoryPort.update(stored);
        Operation op = new Operation();
        op.setOperationType(OperationType.LOAN_CANCELLATION);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(stored);
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", previousStatus);
        details.put("newStatus", LoanStatus.CANCELLED.getCode());
        registerOperationAndAuditService.execute(op, details);
        return stored;
    }
}
