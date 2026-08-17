package application.domain.services.user;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.User;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangeUserPasswordService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;

    public void execute(User user) {
        User stored = userRepositoryPort.findById(user)
                .orElseThrow(() -> new EntityNotFoundException("User"));
        validateStatus(stored);
        String securePassword = passwordServicePort.encrypt(user);
        stored.setPassword(securePassword);
        userRepositoryPort.update(stored);
    }

    private void validateStatus(User user) {
        if (!application.domain.valueobjects.UserStatus.ACTIVE.equals(user.getStatus())) {
            throw new DomainException("Only active users may change their password.");
        }
    }
}
