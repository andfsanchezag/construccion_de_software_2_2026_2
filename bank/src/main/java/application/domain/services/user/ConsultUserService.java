package application.domain.services.user;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultUserService {

    private final UserRepositoryPort userRepositoryPort;

    public User execute(User requestingUser, User user) {
        return userRepositoryPort.findById(user)
                .orElseThrow(() -> new EntityNotFoundException("User"));
    }
}
