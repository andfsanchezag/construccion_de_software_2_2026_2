package application.domain.services.account;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.BankAccount;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.OperationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenBankAccountService {

    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    public BankAccount execute(User requestingUser, BankAccount account) {
        customerRepositoryPort.findByIdentification(account.getOwner())
                .orElseThrow(() -> new EntityNotFoundException("Account owner"));
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setOpeningDate(LocalDate.now());
        BankAccount saved = bankAccountRepositoryPort.save(account);
        registerOpeningOperation(requestingUser, saved);
        return saved;
    }

    private void registerOpeningOperation(User user, BankAccount account) {
        Operation op = new Operation();
        op.setOperationType(OperationType.ACCOUNT_OPENING);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(account);
        registerOperationAndAuditService.execute(op, Map.of("identifier", account.getIdentifier()));
    }
}
