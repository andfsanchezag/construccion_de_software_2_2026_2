package application.domain.services.account;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InsufficientBalanceException;
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
public class WithdrawFundsService {

    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public BankAccount execute(User requestingUser, BankAccount account, BigDecimal amount) {
        BankAccount stored = bankAccountRepositoryPort.findByIdentifier(account)
                .orElseThrow(() -> new EntityNotFoundException("BankAccount"));
        validateCanWithdraw(stored, amount);
        BigDecimal balanceBefore = stored.getCurrentBalance();
        stored.setCurrentBalance(stored.getCurrentBalance().subtract(amount));
        bankAccountRepositoryPort.update(stored);
        registerWithdrawalOperation(requestingUser, stored, amount, balanceBefore);
        return stored;
    }

    private void validateCanWithdraw(BankAccount account, BigDecimal amount) {
        if (!AccountStatus.ACTIVE.equals(account.getAccountStatus())) {
            throw new DomainException("Withdrawals are not allowed on an account with status "
                    + account.getAccountStatus().getCode() + ".");
        }
        if (account.getCurrentBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }
    }

    private void registerWithdrawalOperation(User user, BankAccount account, BigDecimal amount, BigDecimal balanceBefore) {
        Operation op = new Operation();
        op.setOperationType(OperationType.WITHDRAWAL);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(account);
        registerOperationAndAuditService.execute(op, Map.of(
                "amount", amount,
                "balanceBefore", balanceBefore,
                "balanceAfter", account.getCurrentBalance()
        ));
    }
}
