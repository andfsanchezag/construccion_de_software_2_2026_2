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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApproveLoanService {

    private final LoanRepositoryPort loanRepositoryPort;
    private final AuthorizeLoanApprovalService authorizeLoanApprovalService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Loan execute(User user, Loan loan) {
        authorizeLoanApprovalService.execute(user, loan);
        Optional<Loan> storedOpt = loanRepositoryPort.findByIdentifier(loan);
        if (storedOpt.isEmpty()) {
            throw new EntityNotFoundException("Loan");
        }
        Loan stored = storedOpt.get();
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
        Map<String, Object> details = new HashMap<>();
        details.put("approvedAmount", stored.getApprovedAmount());
        details.put("interestRate", stored.getInterestRate());
        details.put("previousStatus", LoanStatus.UNDER_REVIEW.getCode());
        details.put("newStatus", LoanStatus.APPROVED.getCode());
        registerOperationAndAuditService.execute(op, details);
        return stored;
    }
}
