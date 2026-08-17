package application.domain.services.loan;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.out.LoanRepositoryPort;
import application.domain.services.authorization.AuthorizeLoanApprovalService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.LoanStatus;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApproveLoanService {

    private final LoanRepositoryPort loanRepositoryPort;
    private final AuthorizeLoanApprovalService authorizeLoanApprovalService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Loan execute(User user, Loan loan) {
        authorizeLoanApprovalService.execute(user, loan);
        Loan stored = loanRepositoryPort.findByIdentifier(loan)
                .orElseThrow(() -> new EntityNotFoundException("Loan"));
        stored.setLoanStatus(LoanStatus.APPROVED);
        stored.setApprovalDate(LocalDate.now());
        stored.setApprovedAmount(loan.getApprovedAmount());
        stored.setInterestRate(loan.getInterestRate());
        loanRepositoryPort.update(stored);
        Operation op = new Operation();
        op.setOperationType(OperationType.LOAN_APPROVAL);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(stored);
        registerOperationAndAuditService.execute(op, Map.of(
                "approvedAmount", stored.getApprovedAmount(),
                "interestRate", stored.getInterestRate(),
                "previousStatus", LoanStatus.UNDER_REVIEW.getCode(),
                "newStatus", LoanStatus.APPROVED.getCode()
        ));
        return stored;
    }
}
