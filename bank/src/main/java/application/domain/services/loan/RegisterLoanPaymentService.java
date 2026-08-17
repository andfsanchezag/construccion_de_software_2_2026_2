package application.domain.services.loan;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.out.LoanRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegisterLoanPaymentService {

    private final LoanRepositoryPort loanRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Loan execute(User user, Loan loan, BigDecimal amount) {
        Loan stored = loanRepositoryPort.findByIdentifier(loan)
                .orElseThrow(() -> new EntityNotFoundException("Loan"));
        Operation op = new Operation();
        op.setOperationType(OperationType.LOAN_PAYMENT);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(stored);
        registerOperationAndAuditService.execute(op, Map.of("paymentAmount", amount));
        return stored;
    }
}
