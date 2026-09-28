package application.domain.services.user;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Logs out a system user by invalidating all of its issued sessions.
 *
 * <p>Flow (Api-rest-endpoints.md - Logout): the authenticated user is loaded
 * through the UserRepositoryPort and its {@code authTokenVersion} is bumped,
 * so every previously issued token (carrying the old {@code ver} claim) is
 * rejected from then on. Logout is idempotent: unknown users resolve to a
 * no-op at the use-case boundary.
 */
@Service
@RequiredArgsConstructor
public class LogoutService {

    private final UserRepositoryPort userRepositoryPort;

    public void logout(User user) {
        if (user == null || user.getUserId() == null) {
            throw new DomainException("Authenticated user must be provided for logout.");
        }
        Optional<User> storedOpt = userRepositoryPort.findById(user);
        if (storedOpt.isEmpty()) {
            throw new EntityNotFoundException("User");
        }
        User stored = storedOpt.get();
        stored.bumpAuthTokenVersion();
        userRepositoryPort.update(stored);
    }
}
