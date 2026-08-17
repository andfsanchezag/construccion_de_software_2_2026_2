package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Customer;
import application.domain.models.User;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizeCustomerOperationService {

    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;

    public void execute(User user, Customer customer) {
        validateUserAuthorizationStatusService.execute(user);
        if (canAccessAnyCustomer(user)) {
            return;
        }
        if (user.getCustomer() == null
                || !user.getCustomer().getIdentification().equals(customer.getIdentification())) {
            throw new UnauthorizedOperationException("User is not authorized to access this customer.");
        }
    }

    private boolean canAccessAnyCustomer(User user) {
        return SystemRole.TELLER_EMPLOYEE.equals(user.getRole())
                || SystemRole.COMMERCIAL_EMPLOYEE.equals(user.getRole())
                || SystemRole.INTERNAL_ANALYST.equals(user.getRole());
    }
}
