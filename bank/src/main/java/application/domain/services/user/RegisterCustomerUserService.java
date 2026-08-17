package application.domain.services.user;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.User;
import application.domain.models.Customer;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.UserStatus;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterCustomerUserService {

    private final UserRepositoryPort userRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final PasswordServicePort passwordServicePort;

    public User execute(User user) {
        validateCustomerAssociation(user);
        validateUsernameUniqueness(user);
        String securePassword = passwordServicePort.encrypt(user.getPassword());
        user.setPassword(securePassword);
        user.setStatus(UserStatus.ACTIVE);
        return userRepositoryPort.save(user);
    }

    private void validateCustomerAssociation(User user) {
        if (user.getCustomer() == null) {
            throw new DomainException("A customer association is required for customer user registration.");
        }
        Optional<Customer> customerOpt = customerRepositoryPort.findByIdentification(user.getCustomer());
        if (customerOpt.isEmpty()) {
            throw new EntityNotFoundException("Associated customer");
        }
    }

    private void validateUsernameUniqueness(User user) {
        if (userRepositoryPort.existsByUsername(user)) {
            throw new DomainException("Username " + user.getUsername() + " is already in use.");
        }
    }
}
