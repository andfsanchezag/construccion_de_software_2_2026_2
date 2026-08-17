package application.domain.services.user;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.authorization.ValidateUserAuthorizationStatusService;
import application.domain.valueobjects.UserStatus;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangeUserStatusService {

    private final UserRepositoryPort userRepositoryPort;
    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;

    public User execute(User requestingUser, User user, UserStatus newStatus) {
        validateUserAuthorizationStatusService.execute(requestingUser);
        Optional<User> storedOpt = userRepositoryPort.findById(user);
        if (storedOpt.isEmpty()) {
            throw new EntityNotFoundException("User");
        }
        User stored = storedOpt.get();
        validateTransition(stored.getStatus(), newStatus);
        stored.setStatus(newStatus);
        userRepositoryPort.update(stored);
        return stored;
    }

    private void validateTransition(UserStatus current, UserStatus next) {
        boolean valid = (current.equals(UserStatus.ACTIVE) && next.equals(UserStatus.INACTIVE))
                || (current.equals(UserStatus.ACTIVE) && next.equals(UserStatus.BLOCKED))
                || (current.equals(UserStatus.BLOCKED) && next.equals(UserStatus.ACTIVE))
                || (current.equals(UserStatus.INACTIVE) && next.equals(UserStatus.ACTIVE));
        if (!valid) {
            throw new InvalidStatusTransitionException(current.getCode(), next.getCode());
        }
    }
}
