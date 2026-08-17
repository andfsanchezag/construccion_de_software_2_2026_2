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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RejectLoanService {

    private final LoanRepositoryPort loanRepositoryPort;
    private final ValidateInternalAnalystAuthorizationService validateInternalAnalystAuthorizationService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Loan execute(User user, Loan loan) {
        validateInternalAnalystAuthorizationService.execute(user);
        Optional<Loan> storedOpt = loanRepositoryPort.findByIdentifier(loan);
        if (storedOpt.isEmpty()) {
            throw new EntityNotFoundException("Loan");
        }
        Loan stored = storedOpt.get();
        String previousStatus = stored.getLoanStatus().getCode();
        stored.setLoanStatus(LoanStatus.REJECTED);
        loanRepositoryPort.update(stored);
        Operation op = new Operation();
        op.setOperationType(OperationType.LOAN_REJECTION);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(stored);
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", previousStatus);
        details.put("newStatus", LoanStatus.REJECTED.getCode());
        registerOperationAndAuditService.execute(op, details);
        return stored;
    }
}
