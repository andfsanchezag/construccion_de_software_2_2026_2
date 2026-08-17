package application.domain.services.authorization;

import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizeLoanOperationService {

    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;

    public void execute(User user, Loan loan, Operation operation) {
        validateUserAuthorizationStatusService.execute(user);
    }
}
