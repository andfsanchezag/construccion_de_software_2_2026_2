package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedCustomerOperationException;
import application.domain.models.Customer;
import application.domain.models.User;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Authorizes a User to register a Customer according to the customer
 * registration policy.
 *
 * <p>Banking employees (TELLER_EMPLOYEE / COMMERCIAL_EMPLOYEE) may register
 * customers, and a customer user may register the customer associated with
 * their {@code User.customer} (self registration). Other actors are not
 * authorized. The registration policy decision stays in this authorization
 * service and is not duplicated by the Customer Services.
 */
@Service
@RequiredArgsConstructor
public class AuthorizeCustomerRegistrationService {

    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;

    public void execute(User user, Customer customer) {
        if (user == null) {
            throw new UnauthorizedCustomerOperationException("Requesting user must be provided.");
        }
        if (customer == null) {
            throw new UnauthorizedCustomerOperationException("Customer must be provided.");
        }
        validateUserAuthorizationStatusService.execute(user);
        if (isRegistrationEmployee(user)) {
            return;
        }
        if (user.getCustomer() != null
                && customer.getIdentification() != null
                && user.getCustomer().getIdentification().equals(customer.getIdentification())) {
            return;
        }
        throw new UnauthorizedCustomerOperationException(
                "User is not authorized to register the given customer.");
    }

    private boolean isRegistrationEmployee(User user) {
        return SystemRole.TELLER_EMPLOYEE.equals(user.getRole())
                || SystemRole.COMMERCIAL_EMPLOYEE.equals(user.getRole());
    }
}
