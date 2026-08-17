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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepositFundsService {

    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public BankAccount execute(User requestingUser, BankAccount account, BigDecimal amount) {
        Optional<BankAccount> storedOpt = bankAccountRepositoryPort.findByIdentifier(account);
        if (storedOpt.isEmpty()) {
            throw new EntityNotFoundException("BankAccount");
        }
        BankAccount stored = storedOpt.get();
        validateCanDeposit(stored);
        BigDecimal balanceBefore = stored.getCurrentBalance();
        stored.setCurrentBalance(stored.getCurrentBalance().add(amount));
        bankAccountRepositoryPort.update(stored);
        registerDepositOperation(requestingUser, stored, amount, balanceBefore);
        return stored;
    }

    private void validateCanDeposit(BankAccount account) {
        if (!AccountStatus.ACTIVE.equals(account.getAccountStatus())) {
            throw new DomainException("Deposits are not allowed on an account with status "
                    + account.getAccountStatus().getCode() + ".");
        }
    }

    private void registerDepositOperation(User user, BankAccount account, BigDecimal amount, BigDecimal balanceBefore) {
        Operation op = new Operation();
        op.setOperationType(OperationType.DEPOSIT);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(account);
        Map<String, Object> details = new HashMap<>();
        details.put("amount", amount);
        details.put("balanceBefore", balanceBefore);
        details.put("balanceAfter", account.getCurrentBalance());
        registerOperationAndAuditService.execute(op, details);
    }
}
