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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UnblockBankAccountService {

    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public BankAccount execute(User requestingUser, BankAccount account) {
        Optional<BankAccount> storedOpt = bankAccountRepositoryPort.findByIdentifier(account);
        if (storedOpt.isEmpty()) {
            throw new EntityNotFoundException("BankAccount");
        }
        BankAccount stored = storedOpt.get();
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
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", AccountStatus.BLOCKED.getCode());
        registerOperationAndAuditService.execute(op, details);
        return stored;
    }
}
