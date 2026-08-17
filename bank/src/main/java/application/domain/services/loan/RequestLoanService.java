package application.domain.services.loan;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.ports.out.LoanRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.LoanStatus;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RequestLoanService {

    private final LoanRepositoryPort loanRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Loan execute(Loan loan) {
        customerRepositoryPort.findByIdentification(loan.getApplicant())
                .orElseThrow(() -> new EntityNotFoundException("Loan applicant"));
        if (loan.getDestinationAccount() != null) {
            bankAccountRepositoryPort.findByIdentifier(loan.getDestinationAccount())
                    .orElseThrow(() -> new EntityNotFoundException("Destination account"));
        }
        validateRequestedAmount(loan);
        loan.setLoanStatus(LoanStatus.UNDER_REVIEW);
        Loan saved = loanRepositoryPort.save(loan);
        Operation op = new Operation();
        op.setOperationType(OperationType.LOAN_APPLICATION);
        op.setExecutionDate(LocalDateTime.now());
        op.setAffectedProduct(saved);
        registerOperationAndAuditService.execute(op, Map.of(
                "requestedAmount", loan.getRequestedAmount(),
                "loanType", loan.getLoanType().getCode()
        ));
        return saved;
    }

    private void validateRequestedAmount(Loan loan) {
        if (loan.getRequestedAmount() == null || loan.getRequestedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Requested amount must be greater than zero.");
        }
    }
}
