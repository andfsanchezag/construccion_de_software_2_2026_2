package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import application.domain.valueobjects.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Validates that a User is operationally ACTIVE before performing protected
 * operations (user-authentication-services.md - Step 3: Validate User Status).
 */
@Service
@RequiredArgsConstructor
public class ValidateUserAuthorizationStatusService {

    public void execute(User user) {
        if (user == null) {
            throw new UnauthorizedOperationException("User must be provided.");
        }
        if (!UserStatus.ACTIVE.equals(user.getStatus())) {
            throw new UnauthorizedOperationException(
                    "User " + user.getUsername() + " is not active and cannot perform operations.");
        }
    }
}
