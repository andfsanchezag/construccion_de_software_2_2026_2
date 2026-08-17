package application.domain.services.account;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.models.BankAccount;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BlockBankAccountService {

    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public BankAccount execute(User requestingUser, BankAccount account) {
        BankAccount stored = bankAccountRepositoryPort.findByIdentifier(account)
                .orElseThrow(() -> new EntityNotFoundException("BankAccount"));
        if (!AccountStatus.ACTIVE.equals(stored.getAccountStatus())) {
            throw new InvalidStatusTransitionException(stored.getAccountStatus().getCode(), AccountStatus.BLOCKED.getCode());
        }
        stored.setAccountStatus(AccountStatus.BLOCKED);
        bankAccountRepositoryPort.update(stored);
        Operation op = new Operation();
        op.setOperationType(OperationType.ACCOUNT_BLOCKING);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(requestingUser);
        op.setAffectedProduct(stored);
        registerOperationAndAuditService.execute(op, Map.of("previousStatus", AccountStatus.ACTIVE.getCode()));
        return stored;
    }
}
