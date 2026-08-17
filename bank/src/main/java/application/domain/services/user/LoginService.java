package application.domain.services.user;

import application.domain.exceptions.InvalidCredentialsException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.DomainException;
import application.domain.models.User;
import application.domain.ports.out.JwtServicePort;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final JwtServicePort jwtServicePort;

    public String execute(User user) {
        User stored = userRepositoryPort.findByUsername(user)
                .orElseThrow(InvalidCredentialsException::new);

        User candidate = buildCandidateForValidation(user, stored);
        if (!passwordServicePort.matches(candidate)) {
            throw new InvalidCredentialsException();
        }

        validateUserStatus(stored);

        return jwtServicePort.generateToken(stored);
    }

    // Builds a User holding the plain-text password alongside the stored hash for comparison.
    private User buildCandidateForValidation(User submitted, User stored) {
        User candidate = new User();
        candidate.setPassword(submitted.getPassword());
        candidate.setUsername(stored.getUsername());
        return candidate;
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
