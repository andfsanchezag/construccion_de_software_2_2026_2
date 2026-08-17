package application.domain.services.authorization;

import application.domain.models.Loan;
import application.domain.models.User;
import application.domain.valueobjects.LoanStatus;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizeLoanApprovalService {

    private final ValidateInternalAnalystAuthorizationService validateInternalAnalystAuthorizationService;

    public void execute(User user, Loan loan) {
        validateInternalAnalystAuthorizationService.execute(user);
        if (!LoanStatus.UNDER_REVIEW.equals(loan.getLoanStatus())) {
            throw new application.domain.exceptions.UnauthorizedOperationException(
                    "Loan must be in UNDER_REVIEW status to be approved.");
        }
    }
}
