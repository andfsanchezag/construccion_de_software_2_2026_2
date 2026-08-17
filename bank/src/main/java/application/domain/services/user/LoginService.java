package application.domain.services.user;

import application.domain.exceptions.InvalidCredentialsException;
import application.domain.exceptions.DomainException;
import application.domain.models.User;
import application.domain.ports.out.JwtServicePort;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final JwtServicePort jwtServicePort;

    public String execute(User user) {
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
