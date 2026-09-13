package application.domain.services.user;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.User;
import application.domain.ports.in.ChangeUserStatusUseCase;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.services.authorization.AuthorizeChangeUserStatusService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Changes the UserStatus of a system user.
 *
 * <p>The requested target status is carried by the supplied User Domain Model
 * ({@code User.status}); the service never receives the status as an isolated
 * parameter (user-authentication-services.md - Change User Status / Input). The
 * authoritative persisted user is loaded first, the actor is authorized, and the
 * transition is validated by the User Domain behavior ({@code changeStatus}).
 * UserStatus is independent from CustomerStatus: this service never changes any
 * Customer status.
 */
@Service
@RequiredArgsConstructor
public class ChangeUserStatusService implements ChangeUserStatusUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final AuthorizeChangeUserStatusService authorizeChangeUserStatusService;

    @Override
    public User changeUserStatus(User requestingUser, User user) {
        validateInput(user);
        User stored = requireExistingUser(user);
        authorizeChangeUserStatusService.execute(requestingUser);
        stored.changeStatus(user.getStatus());
        userRepositoryPort.update(stored);
        return stored;
    }

    private void validateInput(User user) {
        if (user == null) {
            throw new EntityNotFoundException("User");
        }
        if (user.getStatus() == null) {
            throw new application.domain.exceptions.InvalidUserStatusException(
                    "Target user status must be provided.");
        }
    }

    private User requireExistingUser(User user) {
        Optional<User> storedOpt = userRepositoryPort.findById(user);
        if (storedOpt.isEmpty()) {
            throw new EntityNotFoundException("User");
        }
        return storedOpt.get();
    }
}
