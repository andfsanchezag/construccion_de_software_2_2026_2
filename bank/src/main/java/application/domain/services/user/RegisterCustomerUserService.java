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
        Customer resolvedCustomer = resolveCustomerAssociation(user);
        validateRoleCompatibility(user.getRole(), resolvedCustomer);
        applyCustomerPersonData(user, resolvedCustomer);
        user.ensureRegistrationDataComplete();
        validateUsernameUniqueness(user);
        String securePassword = passwordServicePort.encrypt(user.getPassword());
        user.setPassword(securePassword);
        user.setStatus(UserStatus.ACTIVE);
        return userRepositoryPort.save(user);
    }

    private Customer resolveCustomerAssociation(User user) {
        if (user == null) {
            throw new InvalidUserException("User must be provided.");
        }
        if (user.getCustomer() == null) {
            throw new InvalidUserException("A customer association is required for customer user registration.");
        }
        return customerRepositoryPort.findByIdentification(user.getCustomer())
                .orElseThrow(() -> new EntityNotFoundException("Associated customer"));
    }

    private void validateRoleCompatibility(SystemRole role, Customer customer) {
        boolean compatible =
                (customer instanceof NaturalCustomer && SystemRole.NATURAL_CUSTOMER.equals(role))
                        || (customer instanceof BusinessCustomer && SystemRole.BUSINESS_CUSTOMER.equals(role));
        if (!compatible) {
            throw new DomainException(
                    "Role " + (role == null ? "UNDEFINED" : role.getCode())
                            + " is not compatible with the associated customer.");
        }
    }

    /**
     * Guarantees the User row is always fully populated by mirroring the
     * authoritative Customer's Person data instead of trusting the API caller
     * to resupply it (academic completeness guarantee), and replaces the
     * caller-supplied customer reference stub with the fully resolved
     * Customer (correct subtype and data) resolved from persistence.
     */
    private void applyCustomerPersonData(User user, Customer customer) {
        user.setIdentification(customer.getIdentification());
        user.setName(customer.getName());
        user.setEmail(customer.getEmail());
        user.setPhoneNumber(customer.getPhoneNumber());
        user.setAddress(customer.getAddress());
        user.setCustomer(customer);
    }

    private void validateUsernameUniqueness(User user) {
        if (userRepositoryPort.existsByUsername(user)) {
            throw new DomainException("Username " + user.getUsername() + " is already in use.");
        }
    }
}
