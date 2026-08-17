package application.domain.services.loan;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.ports.out.LoanRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.LoanStatus;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EvaluateLoanService {

    private final LoanRepositoryPort loanRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Loan execute(Loan loan) {
        Loan stored = loanRepositoryPort.findByIdentifier(loan)
                .orElseThrow(() -> new EntityNotFoundException("Loan"));
        customerRepositoryPort.findByIdentification(stored.getApplicant())
                .orElseThrow(() -> new EntityNotFoundException("Loan applicant"));
        if (stored.getDestinationAccount() != null) {
            bankAccountRepositoryPort.findByIdentifier(stored.getDestinationAccount())
                    .orElseThrow(() -> new EntityNotFoundException("Destination account"));
        }
        return stored;
    }
}
