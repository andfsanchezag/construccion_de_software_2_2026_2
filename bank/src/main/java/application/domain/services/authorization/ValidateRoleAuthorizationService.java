package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValidateRoleAuthorizationService {

    public void execute(User user, SystemRole requiredRole) {
        if (!requiredRole.equals(user.getRole())) {
            throw new UnauthorizedOperationException(
                    "Role " + requiredRole.getCode() + " is required for this operation.");
        }
    }
}
