package application.domain.services.user;

import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidUserException;
import application.domain.models.BusinessCustomer;
import application.domain.models.Customer;
import application.domain.models.NaturalCustomer;
import application.domain.models.User;
import application.domain.ports.in.RegisterCustomerUserUseCase;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.UserStatus;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Creates a User associated with an existing Customer.
 *
 * <p>The customer association is a Domain relationship ({@code User.customer});
 * it is never received as a primitive identifier. The service validates the
 * Domain Model, verifies the customer existence through the
 * CustomerRepositoryPort, validates role compatibility with the associated
 * customer, checks username uniqueness through the UserRepositoryPort, and
 * processes the password through the PasswordServicePort before persisting
 * (user-authentication-services.md - Register Customer User).
 */
@Service
@RequiredArgsConstructor
public class RegisterCustomerUserService implements RegisterCustomerUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final CustomerRepositoryPort customerRepositoryPort;
    private final PasswordServicePort passwordServicePort;

    @Override
    public User registerCustomerUser(User requestingUser, User user) {
        validateCustomerAssociation(user);
        validateRoleCompatibility(user);
        validateUsernameUniqueness(user);
        String securePassword = passwordServicePort.encrypt(user.getPassword());
        user.setPassword(securePassword);
        user.setStatus(UserStatus.ACTIVE);
        return userRepositoryPort.save(user);
    }

    private void validateCustomerAssociation(User user) {
        if (user == null) {
            throw new InvalidUserException("User must be provided.");
        }
        if (user.getCustomer() == null) {
            throw new InvalidUserException("A customer association is required for customer user registration.");
        }
        Optional<Customer> customerOpt = customerRepositoryPort.findByIdentification(user.getCustomer());
        if (customerOpt.isEmpty()) {
            throw new EntityNotFoundException("Associated customer");
        }
    }

    private void validateRoleCompatibility(User user) {
        SystemRole role = user.getRole();
        Customer customer = user.getCustomer();
        boolean compatible =
                (customer instanceof NaturalCustomer && SystemRole.NATURAL_CUSTOMER.equals(role))
                        || (customer instanceof BusinessCustomer && SystemRole.BUSINESS_CUSTOMER.equals(role));
        if (!compatible) {
            throw new DomainException(
                    "Role " + (role == null ? "UNDEFINED" : role.getCode())
                            + " is not compatible with the associated customer.");
        }
    }

    private void validateUsernameUniqueness(User user) {
        if (userRepositoryPort.existsByUsername(user)) {
            throw new DomainException("Username " + user.getUsername() + " is already in use.");
        }
    }
}
