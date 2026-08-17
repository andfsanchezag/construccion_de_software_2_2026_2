package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.TransferStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValidateBusinessSupervisorAuthorizationService {

    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;

    public void execute(User user, Transfer transfer) {
        validateUserAuthorizationStatusService.execute(user);
        if (!SystemRole.BUSINESS_SUPERVISOR.equals(user.getRole())) {
            throw new UnauthorizedOperationException("BUSINESS_SUPERVISOR role is required for transfer approval.");
        }
        if (!TransferStatus.WAITING_FOR_APPROVAL.equals(transfer.getTransferStatus())) {
            throw new UnauthorizedOperationException("Transfer is not awaiting approval.");
        }
    }
}
