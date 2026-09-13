package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedCustomerOperationException;
import application.domain.models.User;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Authorizes a User to change the CustomerStatus of a customer.
 *
 * <p>Only authorized banking actors may change customer status. This service
 * centralizes the role-based decision so Customer Services do not duplicate
 * permission rules (customer-services.md 11.2 / 16).
 */
@Service
@RequiredArgsConstructor
public class AuthorizeChangeCustomerStatusService {

    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;

    public void execute(User user) {
        if (user == null) {
            throw new UnauthorizedCustomerOperationException("Requesting user must be provided.");
        }
        validateUserAuthorizationStatusService.execute(user);
        if (!SystemRole.TELLER_EMPLOYEE.equals(user.getRole())
                && !SystemRole.COMMERCIAL_EMPLOYEE.equals(user.getRole())) {
            throw new UnauthorizedCustomerOperationException(
                    "User is not authorized to change customer status.");
        }
    }
}
