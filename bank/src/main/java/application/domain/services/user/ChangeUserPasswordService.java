package application.domain.services.user;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.User;
import application.domain.ports.in.ChangeUserPasswordUseCase;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.UserStatus;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Changes the password of an existing system user.
 *
 * <p>Flow (user-authentication-services.md - Change User Password): validate the
 * user through the UserRepositoryPort, validate the User status, and process the
 * new password through the PasswordServicePort. The plain-text password is never
 * persisted. The supplied User is the actor changing its own password.
 */
@Service
@RequiredArgsConstructor
public class ChangeUserPasswordService implements ChangeUserPasswordUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;

    @Override
    public void changePassword(User user) {
        if (user == null) {
            throw new DomainException("User must be provided.");
        }
        Optional<User> storedOpt = userRepositoryPort.findById(user);
        if (storedOpt.isEmpty()) {
            throw new EntityNotFoundException("User");
        }
        User stored = storedOpt.get();
        validateStatus(stored);
        String securePassword = passwordServicePort.encrypt(user.getPassword());
        stored.setPassword(securePassword);
        userRepositoryPort.update(stored);
    }

    private void validateStatus(User user) {
        if (!UserStatus.ACTIVE.equals(user.getStatus())) {
            throw new DomainException("Only active users may change their password.");
        }
    }
}
