package application.domain.services.authorization;

import application.domain.models.Transfer;
import application.domain.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizeTransferApprovalService {

    private final ValidateBusinessSupervisorAuthorizationService validateBusinessSupervisorAuthorizationService;

    public void execute(User user, Transfer transfer) {
        validateBusinessSupervisorAuthorizationService.execute(user, transfer);
    }
}
