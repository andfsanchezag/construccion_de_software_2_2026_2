package application.domain.services.authorization;

import application.domain.models.Operation;
import application.domain.models.Transfer;
import application.domain.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizeTransferOperationService {

    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;

    public void execute(User user, Transfer transfer, Operation operation) {
        validateUserAuthorizationStatusService.execute(user);
    }
}
