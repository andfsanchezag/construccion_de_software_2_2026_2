package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import application.domain.valueobjects.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValidateUserAuthorizationStatusService {

    public void execute(User user) {
        if (!UserStatus.ACTIVE.equals(user.getStatus())) {
            throw new UnauthorizedOperationException(
                    "User " + user.getUsername() + " is not active and cannot perform operations.");
        }
    }
}
