package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.BankAccount;
import application.domain.models.User;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.valueobjects.SystemRole;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValidateCustomerOwnershipService {

    private final BankAccountRepositoryPort bankAccountRepositoryPort;

    public void execute(User user, BankAccount account) {
        if (isEmployee(user)) {
            return;
        }
        if (user.getCustomer() == null) {
            throw new UnauthorizedOperationException("User has no associated customer.");
        }
        Optional<BankAccount> persistedOpt = bankAccountRepositoryPort.findByIdentifier(account);
        if (persistedOpt.isEmpty()) {
            throw new UnauthorizedOperationException("Bank account not found.");
        }
        BankAccount persisted = persistedOpt.get();
        if (!persisted.getOwner().getIdentification().equals(user.getCustomer().getIdentification())) {
            throw new UnauthorizedOperationException("User is not the owner of this bank account.");
        }
    }

    private boolean isEmployee(User user) {
        return SystemRole.TELLER_EMPLOYEE.equals(user.getRole())
                || SystemRole.COMMERCIAL_EMPLOYEE.equals(user.getRole())
                || SystemRole.INTERNAL_ANALYST.equals(user.getRole());
    }
}
