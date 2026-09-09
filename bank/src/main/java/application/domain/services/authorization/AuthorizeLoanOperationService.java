package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Loan;
import application.domain.models.User;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Authorizes a User to access or operate a Loan.
 *
 * <p>Employees are authorized according to their {@code SystemRole}. Customers are
 * authorized only for loans where {@code User.customer} corresponds to
 * {@code Loan.applicant}. User status must always be ACTIVE.
 */
@Service
@RequiredArgsConstructor
public class AuthorizeLoanOperationService {

    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;

    public void execute(User user, Loan loan) {
        if (user == null) {
            throw new UnauthorizedOperationException("Requesting user must be provided.");
        }
        validateUserAuthorizationStatusService.execute(user);
        if (loan == null) {
            throw new UnauthorizedOperationException("Loan must be provided.");
        }
        if (isEmployee(user)) {
            return;
        }
        if (user.getCustomer() == null
                || loan.getApplicant() == null
                || !user.getCustomer().getIdentification().equals(loan.getApplicant().getIdentification())) {
            throw new UnauthorizedOperationException("User is not authorized to access this loan.");
        }
    }

    private boolean isEmployee(User user) {
        return SystemRole.TELLER_EMPLOYEE.equals(user.getRole())
                || SystemRole.COMMERCIAL_EMPLOYEE.equals(user.getRole())
                || SystemRole.INTERNAL_ANALYST.equals(user.getRole());
    }
}
