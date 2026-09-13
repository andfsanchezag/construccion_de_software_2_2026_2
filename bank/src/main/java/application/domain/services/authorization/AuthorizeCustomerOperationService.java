package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedCustomerOperationException;
import application.domain.models.Customer;
import application.domain.models.User;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Authorizes a User to access or operate over a Customer, matching the customer
 * consultation matrix (customer-services.md 9.7 / 16).
 *
 * <p>Employees (TELLER_EMPLOYEE, COMMERCIAL_EMPLOYEE, INTERNAL_ANALYST) may access
 * any customer. Customer-associated users may access only the customer represented
 * by {@code User.customer}. The User must always be operationally active.
 */
@Service
@RequiredArgsConstructor
public class AuthorizeCustomerOperationService {

    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;

    public void execute(User user, Customer customer) {
        if (user == null) {
            throw new UnauthorizedCustomerOperationException("Requesting user must be provided.");
        }
        if (customer == null) {
            throw new UnauthorizedCustomerOperationException("Customer must be provided.");
        }
        validateUserAuthorizationStatusService.execute(user);
        if (canAccessAnyCustomer(user)) {
            return;
        }
        if (user.getCustomer() == null
                || customer.getIdentification() == null
                || !user.getCustomer().getIdentification().equals(customer.getIdentification())) {
            throw new UnauthorizedCustomerOperationException("User is not authorized to access this customer.");
        }
    }

    private boolean canAccessAnyCustomer(User user) {
        return SystemRole.TELLER_EMPLOYEE.equals(user.getRole())
                || SystemRole.COMMERCIAL_EMPLOYEE.equals(user.getRole())
                || SystemRole.INTERNAL_ANALYST.equals(user.getRole());
    }
}
