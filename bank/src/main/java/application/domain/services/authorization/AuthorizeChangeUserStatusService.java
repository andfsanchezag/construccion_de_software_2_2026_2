package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Authorizes a User to change the UserStatus of another system user.
 *
 * <p>Only active banking employees may perform user status changes; the role
 * decision stays centralized in this authorization service so user services do
 * not duplicate permission rules (user-authentication-services.md - Change User
 * Status / Authorization).
 */
@Service
@RequiredArgsConstructor
public class AuthorizeChangeUserStatusService {

    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;

    public void execute(User requestingUser) {
        if (requestingUser == null) {
            throw new UnauthorizedOperationException("Requesting user must be provided.");
        }
        validateUserAuthorizationStatusService.execute(requestingUser);
        if (!SystemRole.TELLER_EMPLOYEE.equals(requestingUser.getRole())
                && !SystemRole.COMMERCIAL_EMPLOYEE.equals(requestingUser.getRole())
                && !SystemRole.INTERNAL_ANALYST.equals(requestingUser.getRole())) {
            throw new UnauthorizedOperationException(
                    "User is not authorized to change user status.");
        }
    }
}