package application.domain.services.user;

import application.domain.exceptions.InvalidCredentialsException;
import application.domain.exceptions.DomainException;
import application.domain.models.User;
import application.domain.ports.in.LoginUseCase;
import application.domain.ports.out.JwtServicePort;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Authenticates a system User.
 *
 * <p>Flow (user-authentication-services.md - Login): search the user through the
 * UserRepositoryPort, validate the password through the PasswordServicePort,
 * validate the UserStatus, and generate the JWT through the JwtServicePort. The
 * Domain never depends on a concrete password hashing or JWT implementation.
 */
@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final JwtServicePort jwtServicePort;

    @Override
    public String login(User user) {
        if (user == null) {
            throw new InvalidCredentialsException();
        }
        Optional<User> storedOpt = userRepositoryPort.findByUsername(user);
        if (storedOpt.isEmpty()) {
            throw new InvalidCredentialsException();
        }
        User stored = storedOpt.get();

        if (!passwordServicePort.matches(user.getPassword(), stored.getPassword())) {
            throw new InvalidCredentialsException();
        }

        validateUserStatus(stored);

        return jwtServicePort.generateToken(stored);
    }

    private void validateUserStatus(User stored) {
        if (UserStatus.BLOCKED.equals(stored.getStatus())) {
            throw new DomainException("User account is blocked.");
        }
        if (UserStatus.INACTIVE.equals(stored.getStatus())) {
            throw new DomainException("User account is inactive.");
        }
    }
}
