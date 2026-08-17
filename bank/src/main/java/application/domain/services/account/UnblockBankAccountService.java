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
public class UnblockBankAccountService {

    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public BankAccount execute(User requestingUser, BankAccount account) {
        BankAccount stored = bankAccountRepositoryPort.findByIdentifier(account)
                .orElseThrow(() -> new EntityNotFoundException("BankAccount"));
        if (!AccountStatus.BLOCKED.equals(stored.getAccountStatus())) {
            throw new InvalidStatusTransitionException(stored.getAccountStatus().getCode(), AccountStatus.ACTIVE.getCode());
        }
        stored.setAccountStatus(AccountStatus.ACTIVE);
        bankAccountRepositoryPort.update(stored);
        Operation op = new Operation();
        op.setOperationType(OperationType.ACCOUNT_UNBLOCKING);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(requestingUser);
        op.setAffectedProduct(stored);
        registerOperationAndAuditService.execute(op, Map.of("previousStatus", AccountStatus.BLOCKED.getCode()));
        return stored;
    }
}
