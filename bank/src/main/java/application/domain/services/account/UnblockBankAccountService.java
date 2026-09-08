package application.domain.services.account;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.BankAccount;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.UnblockBankAccountUseCase;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.services.operation.RegisterOperationAndAuditService;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UnblockBankAccountService implements UnblockBankAccountUseCase {

    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final RegisterOperationAndAuditService registerOperationAndAuditService;

    @Override
    public BankAccount unblock(User requestingUser, BankAccount account) {
        validateUser(requestingUser);

        Optional<BankAccount> storedOpt = bankAccountRepositoryPort.findByIdentifier(account);
        if (storedOpt.isEmpty()) {
            throw new EntityNotFoundException("BankAccount");
        }
        BankAccount stored = storedOpt.get();
        validateAccess(requestingUser, stored);

        stored.unblock();

        bankAccountRepositoryPort.update(stored);
        registerUnblockOperation(requestingUser, stored);
        return stored;
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new UnauthorizedOperationException("Requesting user must be provided.");
        }
        validateUserAuthorizationStatusService.execute(user);
    }

    private void validateAccess(User user, BankAccount stored) {
        if (isEmployee(user)) {
            return;
        }
        if (user.getCustomer() == null
                || stored.getOwner() == null
                || !user.getCustomer().getIdentification().equals(stored.getOwner().getIdentification())) {
            throw new UnauthorizedOperationException("User is not authorized to operate on this bank account.");
        }
    }

    private boolean isEmployee(User user) {
        return SystemRole.TELLER_EMPLOYEE.equals(user.getRole())
                || SystemRole.COMMERCIAL_EMPLOYEE.equals(user.getRole())
                || SystemRole.INTERNAL_ANALYST.equals(user.getRole());
    }

    private void registerUnblockOperation(User user, BankAccount account) {
        Operation op = new Operation();
        op.setOperationType(OperationType.ACCOUNT_UNBLOCKING);
        op.setExecutionDate(LocalDateTime.now());
        op.setPerformedBy(user);
        op.setAffectedProduct(account);
        Map<String, Object> details = new HashMap<>();
        details.put("previousStatus", AccountStatus.BLOCKED.getCode());
        registerOperationAndAuditService.execute(op, details);
    }
}
