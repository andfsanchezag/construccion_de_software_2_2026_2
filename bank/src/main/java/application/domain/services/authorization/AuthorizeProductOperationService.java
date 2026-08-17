package application.domain.services.authorization;

import application.domain.models.BankingProduct;
import application.domain.models.Operation;
import application.domain.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizeProductOperationService {

    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;

    public void execute(User user, BankingProduct product, Operation operation) {
        validateUserAuthorizationStatusService.execute(user);
    }
}
