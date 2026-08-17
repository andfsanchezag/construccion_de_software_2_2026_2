package application.domain.services.loan;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.out.LoanRepositoryPort;
import application.domain.services.authorization.ValidateInternalAnalystAuthorizationService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.LoanStatus;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RejectLoanService {

    private final LoanRepositoryPort loanRepositoryPort;
    private final ValidateInternalAnalystAuthorizationService validateInternalAnalystAuthorizationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Loan execute(User user, Loan loan) {
        validateInternalAnalystAuthorizationService.execute(user);
        Loan stored = loanRepositoryPort.findByIdentifier(loan)
                .orElseThrow(() -> new EntityNotFoundException("Loan"));
        String previousStatus = stored.getLoanStatus().getCode();
        stored.setLoanStatus(LoanStatus.REJECTED);
        loanRepositoryPort.update(stored);
        Operation op = new Operation();
        op.setOperationType(OperationType.LOAN_REJECTION);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(stored);
        registerOperationAndAuditService.execute(op, Map.of(
                "previousStatus", previousStatus,
                "newStatus", LoanStatus.REJECTED.getCode()
        ));
        return stored;
    }
}
