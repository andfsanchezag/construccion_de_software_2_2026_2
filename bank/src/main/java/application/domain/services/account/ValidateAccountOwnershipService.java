package application.domain.services.account;

import application.domain.models.BankAccount;
import application.domain.models.User;
import application.domain.services.authorization.ValidateCustomerOwnershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValidateAccountOwnershipService {

    private final ValidateCustomerOwnershipService validateCustomerOwnershipService;

    public void execute(User user, BankAccount account) {
        validateCustomerOwnershipService.execute(user, account);
    }
}
