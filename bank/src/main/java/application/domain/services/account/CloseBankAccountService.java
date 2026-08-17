package application.domain.services.account;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.BankAccount;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloseBankAccountService {

    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public BankAccount execute(User requestingUser, BankAccount account) {
        BankAccount stored = bankAccountRepositoryPort.findByIdentifier(account)
                .orElseThrow(() -> new EntityNotFoundException("BankAccount"));
        validateCanClose(stored);
        stored.setAccountStatus(AccountStatus.CLOSED);
        bankAccountRepositoryPort.update(stored);
        Operation op = new Operation();
        op.setOperationType(OperationType.ACCOUNT_CLOSING);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(requestingUser);
        op.setAffectedProduct(stored);
        registerOperationAndAuditService.execute(op, Map.of("finalBalance", stored.getCurrentBalance()));
        return stored;
    }

    private void validateCanClose(BankAccount account) {
        if (AccountStatus.CLOSED.equals(account.getAccountStatus())) {
            throw new DomainException("Account is already closed.");
        }
        if (account.getCurrentBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new DomainException("Account cannot be closed with a non-zero balance.");
        }
    }
}
