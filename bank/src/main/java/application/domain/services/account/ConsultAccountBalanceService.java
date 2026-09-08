package application.domain.services.account;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.BankAccount;
import application.domain.models.User;
import application.domain.ports.in.ConsultAccountBalanceUseCase;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConsultAccountBalanceService implements ConsultAccountBalanceUseCase {

    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;

    @Override
    public Money consultBalance(User requestingUser, BankAccount account) {
        validateUser(requestingUser);

        Optional<BankAccount> found = bankAccountRepositoryPort.findByIdentifier(account);
        if (found.isEmpty()) {
            throw new EntityNotFoundException("BankAccount");
        }
        validateAccess(requestingUser, found.get());
        return Money.of(found.get().getCurrentBalance(), found.get().getCurrency());
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
            throw new UnauthorizedOperationException("User is not authorized to access this bank account.");
        }
    }

    private boolean isEmployee(User user) {
        return SystemRole.TELLER_EMPLOYEE.equals(user.getRole())
                || SystemRole.COMMERCIAL_EMPLOYEE.equals(user.getRole())
                || SystemRole.INTERNAL_ANALYST.equals(user.getRole());
    }
}
