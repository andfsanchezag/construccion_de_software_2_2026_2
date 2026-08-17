package application.domain.services.loan;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.BankAccount;
import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.ports.out.LoanRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.AccountStatus;
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
public class DisburseLoanService {

    private final LoanRepositoryPort loanRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public Loan execute(User user, Loan loan) {
        Optional<Loan> storedOpt = loanRepositoryPort.findByIdentifier(loan);
        if (storedOpt.isEmpty()) {
            throw new EntityNotFoundException("Loan");
        }
        Loan stored = storedOpt.get();
        validateCanDisburse(stored);
        Optional<BankAccount> destinationOpt = bankAccountRepositoryPort.findByIdentifier(stored.getDestinationAccount());
        if (destinationOpt.isEmpty()) {
            throw new EntityNotFoundException("Destination account");
        }
        BankAccount destination = destinationOpt.get();
        validateDestinationAccount(stored, destination);
        destination.setCurrentBalance(destination.getCurrentBalance().add(stored.getApprovedAmount()));
        bankAccountRepositoryPort.update(destination);
        stored.setLoanStatus(LoanStatus.DISBURSED);
        stored.setDisbursementDate(LocalDate.now());
        loanRepositoryPort.update(stored);
        Operation op = new Operation();
        op.setOperationType(OperationType.LOAN_DISBURSEMENT);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(stored);
        Map<String, Object> details = new HashMap<>();
        details.put("approvedAmount", stored.getApprovedAmount());
        details.put("destinationAccount", destination.getIdentifier());
        registerOperationAndAuditService.execute(op, details);
        return stored;
    }

    private void validateCanDisburse(Loan loan) {
        if (!LoanStatus.APPROVED.equals(loan.getLoanStatus())) {
            throw new DomainException("Only approved loans can be disbursed.");
        }
    }

    private void validateDestinationAccount(Loan loan, BankAccount account) {
        if (!AccountStatus.ACTIVE.equals(account.getAccountStatus())) {
            throw new DomainException("Destination account must be active for disbursement.");
        }
        if (!account.getOwner().getIdentification().equals(loan.getApplicant().getIdentification())) {
            throw new DomainException("Destination account must belong to the loan applicant.");
        }
    }
}
