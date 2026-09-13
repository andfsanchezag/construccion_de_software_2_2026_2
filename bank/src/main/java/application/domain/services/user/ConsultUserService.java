package application.domain.services.user;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import application.domain.ports.in.ConsultUserUseCase;
import application.domain.ports.out.UserRepositoryPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Retrieves information associated with a system User.
 *
 * <p>Read-only use case (user-authentication-services.md - Consult User): the
 * requesting actor must exist and be operationally ACTIVE; the result remains a
 * Domain Model. Persistence entities are never returned.
 */
@Service
@RequiredArgsConstructor
public class ConsultUserService implements ConsultUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final application.domain.services.authorization.ValidateUserAuthorizationStatusService
            validateUserAuthorizationStatusService;

    @Override
    public User consultUser(User requestingUser, User user) {
        if (requestingUser == null) {
            throw new UnauthorizedOperationException("Requesting user must be provided.");
        }
        if (userRepositoryPort.findById(requestingUser).isEmpty()) {
            throw new UnauthorizedOperationException("Requesting user not found.");
        }
        validateUserAuthorizationStatusService.execute(requestingUser);
        Optional<User> found = userRepositoryPort.findById(user);
        if (found.isEmpty()) {
            throw new EntityNotFoundException("User");
        }
        return found.get();
    }
}
