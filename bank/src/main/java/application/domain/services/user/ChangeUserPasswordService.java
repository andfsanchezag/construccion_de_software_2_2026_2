package application.domain.services.user;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.User;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.UserStatus;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangeUserPasswordService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;

    public void execute(User user) {
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
