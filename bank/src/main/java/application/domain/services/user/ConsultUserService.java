package application.domain.services.user;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultUserService {

    private final UserRepositoryPort userRepositoryPort;

    public User execute(User requestingUser, User user) {
        Optional<User> found = userRepositoryPort.findById(user);
        if (found.isEmpty()) {
            throw new EntityNotFoundException("User");
        }
        return found.get();
    }
}
