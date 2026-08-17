package application.domain.services.authorization;

import application.domain.models.BankAccount;
import application.domain.models.Operation;
import application.domain.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizeBankAccountOperationService {

    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;
    private final ValidateCustomerOwnershipService validateCustomerOwnershipService;

    public void execute(User user, BankAccount account, Operation operation) {
        validateUserAuthorizationStatusService.execute(user);
        validateCustomerOwnershipService.execute(user, account);
    }
}
