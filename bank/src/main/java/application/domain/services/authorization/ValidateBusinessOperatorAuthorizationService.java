package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.BusinessCustomer;
import application.domain.models.BankingProduct;
import application.domain.models.User;
import application.domain.valueobjects.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValidateBusinessOperatorAuthorizationService {

    private final ValidateUserAuthorizationStatusService validateUserAuthorizationStatusService;

    public void execute(User user, BusinessCustomer customer, BankingProduct product) {
        validateUserAuthorizationStatusService.execute(user);
        if (!SystemRole.BUSINESS_OPERATOR.equals(user.getRole())) {
            throw new UnauthorizedOperationException("BUSINESS_OPERATOR role is required.");
        }
        if (user.getCustomer() == null
                || !user.getCustomer().getIdentification().equals(customer.getIdentification())) {
            throw new UnauthorizedOperationException(
                    "Operator is not associated with the requested business customer.");
        }
    }
}
