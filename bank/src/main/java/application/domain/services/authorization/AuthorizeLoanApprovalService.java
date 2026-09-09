package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Authorizes a User to approve (or reject) a loan.
 *
 * <p>Authorization validates both {@code UserStatus} (ACTIVE) and
 * {@code SystemRole} (INTERNAL_ANALYST). The loan status transition itself
 * (UNDER_REVIEW → APPROVED) is not an authorization concern: it is enforced by
 * the {@code Loan} Domain behavior on the authoritative persisted model.
 */
@Service
@RequiredArgsConstructor
public class AuthorizeLoanApprovalService {

    private final ValidateInternalAnalystAuthorizationService validateInternalAnalystAuthorizationService;

    public void execute(User user) {
        if (user == null) {
            throw new UnauthorizedOperationException("Requesting user must be provided.");
        }
        validateInternalAnalystAuthorizationService.execute(user);
    }
}
